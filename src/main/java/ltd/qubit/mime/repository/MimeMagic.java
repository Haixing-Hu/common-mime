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
import java.util.LinkedList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import ltd.qubit.commons.io.serialize.BinarySerialization;
import ltd.qubit.commons.io.serialize.XmlSerialization;
import ltd.qubit.commons.lang.ArrayUtils;
import ltd.qubit.commons.lang.CloneableEx;
import ltd.qubit.commons.text.tostring.ToStringBuilder;

/**
 * The object of this class represents the magic of the file content for a
 * certain MIME type.
 *
 * @see <a href='http://standards.freedesktop.org/shared-mime-info-spec/shared-mime-info-spec-0.19.html'>Shared MIME-info Database</a>
 * @see <a href='http://www.freedesktop.org/wiki/Software/shared-mime-info'>shared-mime-info</a>
 * @author Haixing Hu
 */
@NotThreadSafe
public final class MimeMagic implements Serializable, CloneableEx<MimeMagic> {

  private static final long serialVersionUID = -6497936348325783502L;

  public static final int   MIN_PRIORITY                = 0;
  public static final int   MAX_PRIORITY                = 100;
  public static final int   DEFAULT_PRIORITY            = 50;

  static {
    BinarySerialization.register(MimeMagic.class, MimeMagicBinarySerializer.INSTANCE);
    XmlSerialization.register(MimeMagic.class, MimeMagicXmlSerializer.INSTANCE);
  }

  int priority;
  List<MimeMagicMatcher> matchers;

  public MimeMagic() {
    priority = DEFAULT_PRIORITY;
    matchers = new LinkedList<MimeMagicMatcher>();
  }

  public MimeMagic(final List<MimeMagicMatcher> matchers, final int priority) {
    if (matchers == null) {
      throw new NullPointerException();
    }
    if ((priority < MIN_PRIORITY) || (priority > MAX_PRIORITY)) {
      throw new IllegalArgumentException();
    }
    this.priority = priority;
    this.matchers = matchers;
  }

  public int getPriority() {
    return priority;
  }

  public MimeMagicMatcher[] getMatchers() {
    final int n = matchers.size();
    if (n == 0) {
      return (MimeMagicMatcher[]) ArrayUtils.EMPTY_OBJECT_ARRAY;
    } else {
      return matchers.toArray(new MimeMagicMatcher[n]);
    }
  }

  /**
   * Returns the maximum number of bytes need to be test by this MIME magic.
   *
   * @return The maximum number of bytes need to be test by this MIME magic.
   *         Used to determine the size of the read buffer.
   */
  public int getMaxTestBytes() {
    int result = 0;
    for (final MimeMagicMatcher matcher : matchers) {
      final int matcherMaxTestBytes = matcher.getMaxTestBytes();
      if (result < matcherMaxTestBytes) {
        result = matcherMaxTestBytes;
      }
    }
    return result;
  }

  public boolean matches(final byte[] buffer, final int nBytes) {
    for (final MimeMagicMatcher matcher : matchers) {
      if (matcher.matches(buffer, nBytes)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public MimeMagic clone() {
    final MimeMagic result = new MimeMagic();
    result.priority = priority;
    for (final MimeMagicMatcher matcher : matchers) {
      result.matchers.add(matcher.clone());
    }
    return result;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
               .append("priority", priority)
               .append("matchers", matchers)
               .toString();
  }
}
