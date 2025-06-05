////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import java.io.Serial;
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
 * {@link MimeGlob}对象表示特定MIME类型的文件名模式（或glob）。
 *
 * @see <a href='http://standards.freedesktop.org/shared-mime-info-spec/shared-mime-info-spec-0.19.html'>Shared MIME-info Database</a>
 * @see <a href='http://www.freedesktop.org/wiki/Software/shared-mime-info'>shared-mime-info</a>
 * @author 胡海星
 */
@ThreadSafe
public final class MimeGlob implements Serializable, CloneableEx<MimeGlob> {

  @Serial
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

  /**
   * 构造一个默认的MimeGlob对象。
   */
  public MimeGlob() {
    weight = DEFAULT_WEIGHT;
    caseSensitive = DEFAULT_CASE_SENSITIVE;
    pattern = StringUtils.EMPTY;
    matcher = null;
  }

  /**
   * 构造一个具有指定模式的MimeGlob对象。
   *
   * @param pattern
   *     文件名匹配模式。
   * @throws NullPointerException
   *     如果pattern为null。
   */
  public MimeGlob(final String pattern) {
    if (pattern == null) {
      throw new NullPointerException();
    }
    this.weight = DEFAULT_WEIGHT;
    this.caseSensitive = DEFAULT_CASE_SENSITIVE;
    this.pattern = pattern;
    this.matcher = null;
  }

  /**
   * 构造一个具有指定模式和权重的MimeGlob对象。
   *
   * @param pattern
   *     文件名匹配模式。
   * @param weight
   *     模式的权重。
   * @throws NullPointerException
   *     如果pattern为null。
   * @throws IllegalArgumentException
   *     如果weight不在合法范围内。
   */
  public MimeGlob(final String pattern, final int weight) {
    this(pattern, weight, DEFAULT_CASE_SENSITIVE);
  }

  /**
   * 构造一个具有指定模式、权重和大小写敏感性的MimeGlob对象。
   *
   * @param pattern
   *     文件名匹配模式。
   * @param weight
   *     模式的权重。
   * @param caseSensitive
   *     模式是否区分大小写。
   * @throws NullPointerException
   *     如果pattern为null。
   * @throws IllegalArgumentException
   *     如果weight不在合法范围内。
   */
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

  /**
   * 获取此模式的权重。
   *
   * @return 此模式的权重。
   */
  public int getWeight() {
    return weight;
  }

  /**
   * 判断此模式是否区分大小写。
   *
   * @return 如果此模式区分大小写，则返回true；否则返回false。
   */
  public boolean isCaseSensitive() {
    return caseSensitive;
  }

  /**
   * 获取此模式的字符串表示。
   *
   * @return 此模式的字符串表示。
   */
  public String getPattern() {
    return pattern;
  }

  /**
   * 测试指定的文件名是否与此模式匹配。
   *
   * @param filename
   *     要测试的文件名。
   * @return
   *     如果指定的文件名与此模式匹配，则返回true；否则返回false。
   */
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

  /**
   * 为指定的字符串创建匹配器。
   *
   * @param str
   *     需要创建匹配器的字符串。
   */
  private void createMatcher(@Nonnull final String str) {
    int flags = 0;
    if (! caseSensitive) {
      flags = Pattern.CASE_INSENSITIVE;
    }
    final String regex = Glob.toRegex(pattern);
    matcher = Pattern.compile(regex, flags).matcher(str);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    final int multiplier = 191;
    int code = 1111;
    code = Hash.combine(code, multiplier, weight);
    code = Hash.combine(code, multiplier, caseSensitive);
    code = Hash.combine(code, multiplier, pattern);
    return code;
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public @Nonnull MimeGlob cloneEx() {
    final MimeGlob cloned = new MimeGlob();
    cloned.weight = this.weight;
    cloned.caseSensitive = this.caseSensitive;
    cloned.pattern = this.pattern;
    cloned.matcher = null;
    return cloned;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @Nonnull String toString() {
    return new ToStringBuilder(this)
               .append("weight", weight)
               .append("caseSensitive", caseSensitive)
               .append("pattern", pattern)
               .toString();
  }

}