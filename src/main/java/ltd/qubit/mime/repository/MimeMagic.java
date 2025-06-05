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
import java.util.LinkedList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import ltd.qubit.commons.io.serialize.BinarySerialization;
import ltd.qubit.commons.io.serialize.XmlSerialization;
import ltd.qubit.commons.lang.ArrayUtils;
import ltd.qubit.commons.lang.CloneableEx;
import ltd.qubit.commons.text.tostring.ToStringBuilder;

/**
 * 此类的对象表示特定MIME类型的文件内容魔数。
 *
 * @see <a href='http://standards.freedesktop.org/shared-mime-info-spec/shared-mime-info-spec-0.19.html'>Shared MIME-info Database</a>
 * @see <a href='http://www.freedesktop.org/wiki/Software/shared-mime-info'>shared-mime-info</a>
 * @author 胡海星
 */
@NotThreadSafe
public final class MimeMagic implements Serializable, CloneableEx<MimeMagic> {

  @Serial
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

  /**
   * 构造一个默认的MIME魔数对象。
   */
  public MimeMagic() {
    priority = DEFAULT_PRIORITY;
    matchers = new LinkedList<MimeMagicMatcher>();
  }

  /**
   * 构造一个具有指定匹配器列表和优先级的MIME魔数对象。
   *
   * @param matchers
   *     魔数匹配器列表。
   * @param priority
   *     魔数的优先级。
   * @throws NullPointerException
   *     如果matchers为null。
   * @throws IllegalArgumentException
   *     如果priority不在合法范围内。
   */
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

  /**
   * 获取此魔数的优先级。
   *
   * @return 此魔数的优先级。
   */
  public int getPriority() {
    return priority;
  }

  /**
   * 获取此魔数的匹配器数组。
   *
   * @return 此魔数的匹配器数组。如果没有匹配器，则返回空数组。
   */
  public MimeMagicMatcher[] getMatchers() {
    final int n = matchers.size();
    if (n == 0) {
      return (MimeMagicMatcher[]) ArrayUtils.EMPTY_OBJECT_ARRAY;
    } else {
      return matchers.toArray(new MimeMagicMatcher[n]);
    }
  }

  /**
   * 返回此MIME魔数需要测试的最大字节数。
   *
   * @return 此MIME魔数需要测试的最大字节数。用于确定读取缓冲区的大小。
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

  /**
   * 测试指定的缓冲区是否与此MIME魔数匹配。
   *
   * @param buffer
   *     要测试的缓冲区。
   * @param nBytes
   *     缓冲区中要测试的字节数。
   * @return
   *     如果指定的缓冲区与此MIME魔数匹配，则返回true；否则返回false。
   */
  public boolean matches(final byte[] buffer, final int nBytes) {
    for (final MimeMagicMatcher matcher : matchers) {
      if (matcher.matches(buffer, nBytes)) {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MimeMagic cloneEx() {
    final MimeMagic result = new MimeMagic();
    result.priority = priority;
    for (final MimeMagicMatcher matcher : matchers) {
      result.matchers.add(matcher.cloneEx());
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this)
               .append("priority", priority)
               .append("matchers", matchers)
               .toString();
  }
}