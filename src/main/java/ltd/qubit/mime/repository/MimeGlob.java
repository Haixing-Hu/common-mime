////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2017 - 2024.
//    Nanjing Smart Medical Investment Operation Service Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import ltd.qubit.commons.io.serialize.BinarySerialization;
import ltd.qubit.commons.io.serialize.XmlSerialization;
import ltd.qubit.commons.lang.CloneableEx;
import ltd.qubit.commons.lang.Equality;
import ltd.qubit.commons.lang.Hash;
import ltd.qubit.commons.lang.StringUtils;
import ltd.qubit.commons.text.Glob;
import ltd.qubit.commons.text.tostring.ToStringBuilder;

/**
 * A {@link MimeGlob} object represents the glob (or pattern) of the filename for
 * a certain MIME type.
 *
 * @see <a href='http://standards.freedesktop.org/shared-mime-info-spec/shared-mime-info-spec-0.19.html'>Shared MIME-info Database</a>
 * @see <a href='http://www.freedesktop.org/wiki/Software/shared-mime-info'>shared-mime-info</a>
 * @author Haixing Hu
 */
@ThreadSafe
public final class MimeGlob implements Serializable, CloneableEx<MimeGlob> {

  private static final long serialVersionUID = 7956131472186019862L;

  public static final int     MIN_WEIGHT                = 0;
  public static final int     MAX_WEIGHT                = 100;
  public static final int     DEFAULT_WEIGHT            = 50;
  public static final boolean DEFAULT_CASE_SENSITIVE    = false;

  static {
    BinarySerialization.register(MimeGlob.class, MimeGlobBinarySerializer.INSTANCE);
    XmlSerialization.register(MimeGlob.class, MimeGlobXmlSerializer.INSTANCE);
  }

  int weight;
  boolean caseSensitive;
  @Nonnull
  String pattern;
  @Nullable
  transient Matcher matcher;

  public MimeGlob() {
    weight = DEFAULT_WEIGHT;
    caseSensitive = DEFAULT_CASE_SENSITIVE;
    pattern = StringUtils.EMPTY;
    matcher = null;
  }

  public MimeGlob(final String pattern) {
    if (pattern == null) {
      throw new NullPointerException();
    }
    this.weight = DEFAULT_WEIGHT;
    this.caseSensitive = DEFAULT_CASE_SENSITIVE;
    this.pattern = pattern;
    this.matcher = null;
  }

  public MimeGlob(final String pattern, final int weight) {
    this(pattern, weight, DEFAULT_CASE_SENSITIVE);
  }

  public MimeGlob(final String pattern, final int weight,
      final boolean caseSensitive) {
    if (pattern == null) {
      throw new NullPointerException();
    }
    if ((weight < MIN_WEIGHT) || (weight > MAX_WEIGHT)) {
      throw new IllegalArgumentException();
    }
    this.weight = weight;
    this.caseSensitive = caseSensitive;
    this.pattern = pattern;
    this.matcher = null;
  }

  public int getWeight() {
    return weight;
  }

  public boolean isCaseSensitive() {
    return caseSensitive;
  }

  public String getPattern() {
    return pattern;
  }

  public boolean matches(@Nullable final String filename) {
    if ((filename == null) || (filename.length() == 0)) {
      return false;
    } else if ((pattern == null) || (pattern.length() == 0)) {
      return false;
    }
    synchronized (this) {
      if (matcher == null) {
        createMatcher(filename);
      } else {
        matcher.reset(filename);
      }
      return matcher.matches();
    }
  }

  private void createMatcher(@Nonnull final String str) {
    int flags = 0;
    if (! caseSensitive) {
      flags = Pattern.CASE_INSENSITIVE;
    }
    final String regex = Glob.toRegex(pattern);
    matcher = Pattern.compile(regex, flags).matcher(str);
  }

  @Override
  public int hashCode() {
    final int multiplier = 191;
    int code = 1111;
    code = Hash.combine(code, multiplier, weight);
    code = Hash.combine(code, multiplier, caseSensitive);
    code = Hash.combine(code, multiplier, pattern);
    return code;
  }

  @Override
  public boolean equals(@Nullable final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MimeGlob other = (MimeGlob) obj;
    return (weight == other.weight)
         && (caseSensitive == other.caseSensitive)
         && Equality.equals(pattern, other.pattern);
  }

  @Override
  public @Nonnull MimeGlob clone() {
    final MimeGlob cloned = new MimeGlob();
    cloned.weight = this.weight;
    cloned.caseSensitive = this.caseSensitive;
    cloned.pattern = this.pattern;
    cloned.matcher = null;
    return cloned;
  }

  @Override
  public @Nonnull String toString() {
    return new ToStringBuilder(this)
               .append("weight", weight)
               .append("caseSensitive", caseSensitive)
               .append("pattern", pattern)
               .toString();
  }

}
