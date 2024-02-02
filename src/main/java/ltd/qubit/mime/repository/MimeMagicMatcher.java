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
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import ltd.qubit.commons.io.serialize.BinarySerialization;
import ltd.qubit.commons.io.serialize.XmlSerialization;
import ltd.qubit.commons.lang.ArrayUtils;
import ltd.qubit.commons.lang.Assignment;
import ltd.qubit.commons.lang.CloneableEx;
import ltd.qubit.commons.lang.Equality;
import ltd.qubit.commons.lang.Hash;
import ltd.qubit.commons.text.tostring.ToStringBuilder;

/**
 * A {@link MimeMagicMatcher} object represents the matching rule of the
 * MIME type magic.
 * <p>
 * A MIME type magic can contain multiple matches.
 *
 * @see <a href='http://standards.freedesktop.org/shared-mime-info-spec/shared-mime-info-spec-0.19.html'>Shared MIME-info Database</a>
 * @see <a href='http://www.freedesktop.org/wiki/Software/shared-mime-info'>shared-mime-info</a>
 * @author Haixing Hu
 */
@NotThreadSafe
public final class MimeMagicMatcher implements Serializable, CloneableEx<MimeMagicMatcher> {

  private static final long  serialVersionUID = 3647729130760643950L;

  static final int TYPE_UNKNOWN         = 0;
  static final int TYPE_STRING          = 1;
  static final int TYPE_HOST16          = 2;
  static final int TYPE_HOST32          = 3;
  static final int TYPE_BIG16           = 4;
  static final int TYPE_BIG32           = 5;
  static final int TYPE_LITTLE16        = 6;
  static final int TYPE_LITTLE32        = 7;
  static final int TYPE_BYTE            = 8;

  static final String[] TYPE_NAMES = {
    "unknown",
    "string",
    "host16",
    "host32",
    "big16",
    "big32",
    "little16",
    "little32",
    "byte"
  };

  static {
    BinarySerialization.register(MimeMagicMatcher.class, MimeMagicMatcherBinarySerializer.INSTANCE);
    XmlSerialization.register(MimeMagicMatcher.class, MimeMagicMatcherXmlSerializer.INSTANCE);
  }

  // the value type of this matcher.
  int     type;
  // search the match in [m_offsetBegin, m_offsetEnd].
  int     offsetBegin;
  int     offsetEnd;
  // value and mask are always stored as a byte array, in big endian.
  byte[]  value;
  byte[]  mask;
  // stores the list of sub-matchers.
  List<MimeMagicMatcher> subMatchers;

  public MimeMagicMatcher() {
    this.type = TYPE_UNKNOWN;
    this.offsetBegin = -1;
    this.offsetEnd = -1;
    this.value = null;
    this.mask = null;
    this.subMatchers = new LinkedList<MimeMagicMatcher>();
  }

  public int getType() {
    return type;
  }

  public int getOffsetBegin() {
    return offsetBegin;
  }

  public int getOffsetEnd() {
    return offsetEnd;
  }

  public byte[] getValue() {
    return value;
  }

  public byte[] getMask() {
    return mask;
  }

  public MimeMagicMatcher[] getSubMatchers() {
    final int n = subMatchers.size();
    if (n == 0) {
      return (MimeMagicMatcher[]) ArrayUtils.EMPTY_OBJECT_ARRAY;
    } else {
      return subMatchers.toArray(new MimeMagicMatcher[n]);
    }
  }

  /**
   * Returns the maximum number of bytes need to be test by this matcher.
   *
   * @return The maximum number of bytes need to be test by this matcher. Used
   *         to determine the size of the read buffer.
   */
  public int getMaxTestBytes() {
    int result = offsetEnd + value.length;
    for (final MimeMagicMatcher subMatcher : subMatchers) {
      final int subMatcherMaxTestBytes = subMatcher.getMaxTestBytes();
      if (result < subMatcherMaxTestBytes) {
        result = subMatcherMaxTestBytes;
      }
    }
    return result;
  }

  public boolean matches(final byte[] buffer, final int nBytes) {
    if ((nBytes < 0) || (nBytes > buffer.length)) {
      throw new IllegalArgumentException();
    }
    // Check the requirement of this matcher
    boolean reverseOrder = false;
    switch (type) {
      case TYPE_STRING:
        if (! matchesString(buffer, nBytes)) {
          return false;
        }
        break;
      case TYPE_HOST16:
        reverseOrder = (ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN);
        if (!matchesInt16(buffer, nBytes, reverseOrder)) {
          return false;
        }
        break;
      case TYPE_HOST32:
        reverseOrder = (ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN);
        if (!matchesInt32(buffer, nBytes, reverseOrder)) {
          return false;
        }
        break;
      case TYPE_BIG16:
        if (!matchesInt16(buffer, nBytes, false)) {
          return false;
        }
        break;
      case TYPE_BIG32:
        if (!matchesInt32(buffer, nBytes, false)) {
          return false;
        }
        break;
      case TYPE_LITTLE16:
        if (!matchesInt16(buffer, nBytes, true)) {
          return false;
        }
        break;
      case TYPE_LITTLE32:
        if (!matchesInt32(buffer, nBytes, true)) {
          return false;
        }
        break;
      case TYPE_BYTE:
        if (!matchesByte(buffer, nBytes)) {
          return false;
        }
        break;
      default:
        return false;
    }
    // Now we know that the requirement of this matcher is satisfied.
    // If it has no children matches, just returns true.
    if (subMatchers.isEmpty()) {
      return true;
    }
    // Otherwise, this matcher has child matcher, and it is
    // matched if and only if there is any sub-matcher matches
    boolean result = false;
    for (final MimeMagicMatcher matcher : subMatchers) {
      assert (matcher != null);
      if (matcher.matches(buffer, nBytes)) {
        result = true;
        break;
      }
    }
    return result;
  }

  private boolean matchesByte(final byte[] buffer, final int nBytes) {
    if ((mask == null) || (mask.length == 0)) {
      final byte v = value[0];
      final int end = (offsetEnd < nBytes ? offsetEnd : nBytes - 1);
      for (int offset = offsetBegin; offset <= end; ++offset) {
        if (buffer[offset] == v) {
          return true;
        }
      }
    } else {
      final int v = (value[0] & 0xFF);
      final int m = (mask[0] & 0xFF);
      final int end = (offsetEnd < nBytes ? offsetEnd : nBytes - 1);
      for (int offset = offsetBegin; offset <= end; ++offset) {
        if ((buffer[offset] & m) == v) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean matchesString(final byte[] buffer, final int nBytes) {
    if ((mask == null) || (mask.length == 0)) {
      int end = nBytes - value.length;
      if (end > offsetEnd) {
        end = offsetEnd;
      }
      for (int offset = offsetBegin; offset <= end; ++offset) {
        boolean result = true;
        for (int i = 0; i < value.length; ++i) {
          if (buffer[offset + i] != value[i]) {
            result = false;
            break;
          }
        }
        if (result) { // find a match at current offset
          return true;
        }
      }
    } else {
      int end = nBytes - value.length;
      if (end > offsetEnd) {
        end = offsetEnd;
      }
      for (int offset = offsetBegin; offset <= end; ++offset) {
        boolean result = true;
        for (int i = 0; i < value.length; ++i) {
          if ((buffer[offset + i] & mask[i]) != value[i]) {
            result = false;
            break;
          }
        }
        if (result) { // find a match at current offset
          return true;
        }
      }
    }
    return false;
  }

  private boolean matchesInt16(final byte[] buffer, final int nBytes, final boolean reverseOrder) {
    if ((mask == null) || (mask.length == 0)) {
      final byte v0;
      final byte v1;
      if (reverseOrder) {
        v0 = value[1];
        v1 = value[0];
      } else {
        v0 = value[0];
        v1 = value[1];
      }
      int end = nBytes - 2;
      if (end > offsetEnd) {
        end = offsetEnd;
      }
      for (int offset = offsetBegin; offset <= end; ++offset) {
        if ((buffer[offset] == v0) && (buffer[offset + 1] == v1)) {
          // find a match at current offset
          return true;
        }
      }
    } else {
      final int v0;
      final int v1;
      final int m0;
      final int m1;
      if (reverseOrder) {
        v0 = (value[1] & 0xFF);
        v1 = (value[0] & 0xFF);
        m0 = (mask[1] & 0xFF);
        m1 = (mask[0] & 0xFF);
      } else {
        v0 = (value[0] & 0xFF);
        v1 = (value[1] & 0xFF);
        m0 = (mask[0] & 0xFF);
        m1 = (mask[1] & 0xFF);
      }
      int end = nBytes - 2;
      if (end > offsetEnd) {
        end = offsetEnd;
      }
      for (int offset = offsetBegin; offset <= end; ++offset) {
        if (((buffer[offset] & m0) == v0) && ((buffer[offset + 1] & m1) == v1)) {
          // find a match at current offset
          return true;
        }
      }
    }
    return false;
  }

  private boolean matchesInt32(final byte[] buffer, final int nBytes, final boolean reverseOrder) {
    if ((mask == null) || (mask.length == 0)) {
      final byte v0;
      final byte v1;
      final byte v2;
      final byte v3;
      if (reverseOrder) {
        v0 = value[3];
        v1 = value[2];
        v2 = value[1];
        v3 = value[0];
      } else {
        v0 = value[0];
        v1 = value[1];
        v2 = value[2];
        v3 = value[3];
      }
      int end = nBytes - 4;
      if (end > offsetEnd) {
        end = offsetEnd;
      }
      for (int offset = offsetBegin; offset <= end; ++offset) {
        if ((buffer[offset] == v0) && (buffer[offset + 1] == v1)
            && (buffer[offset + 2] == v2) && (buffer[offset + 3] == v3)) {
          // find a match at current offset
          return true;
        }
      }
    } else {
      assert (mask.length == 4);
      final int v0;
      final int v1;
      final int v2;
      final int v3;
      final int m0;
      final int m1;
      final int m2;
      final int m3;
      if (reverseOrder) {
        v0 = (value[3] & 0xFF);
        v1 = (value[2] & 0xFF);
        v2 = (value[1] & 0xFF);
        v3 = (value[0] & 0xFF);
        m0 = (mask[3] & 0xFF);
        m1 = (mask[2] & 0xFF);
        m2 = (mask[1] & 0xFF);
        m3 = (mask[0] & 0xFF);
      } else {
        v0 = (value[0] & 0xFF);
        v1 = (value[1] & 0xFF);
        v2 = (value[2] & 0xFF);
        v3 = (value[3] & 0xFF);
        m0 = (mask[0] & 0xFF);
        m1 = (mask[1] & 0xFF);
        m2 = (mask[2] & 0xFF);
        m3 = (mask[3] & 0xFF);
      }
      int end = nBytes - 4;
      if (end > offsetEnd) {
        end = offsetEnd;
      }
      for (int offset = offsetBegin; offset <= end; ++offset) {
        if (((buffer[offset] & m0) == v0) && ((buffer[offset + 1] & m1) == v1)
            && ((buffer[offset + 2] & m2) == v2)
            && ((buffer[offset + 3] & m3) == v3)) {
          // find a match at current offset
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int multiplier = 191;
    int code = 1111;
    code = Hash.combine(code, multiplier, type);
    code = Hash.combine(code, multiplier, offsetBegin);
    code = Hash.combine(code, multiplier, offsetEnd);
    code = Hash.combine(code, multiplier, value);
    code = Hash.combine(code, multiplier, mask);
    code = Hash.combine(code, multiplier, subMatchers);
    return code;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MimeMagicMatcher other = (MimeMagicMatcher) obj;
    return (type == other.type)
          && (offsetBegin == other.offsetBegin)
          && Equality.equals(value, other.value)
          && Equality.equals(mask, other.mask)
          && Equality.equals(subMatchers, other.subMatchers);
  }

  @Override
  public MimeMagicMatcher clone() {
    final MimeMagicMatcher result = new MimeMagicMatcher();
    result.type = type;
    result.offsetBegin = offsetBegin;
    result.offsetEnd = offsetEnd;
    result.value = Assignment.clone(value);
    result.mask = Assignment.clone(mask);
    for (final MimeMagicMatcher matcher : subMatchers) {
      result.subMatchers.add(matcher.clone());
    }
    return result;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
               .append("type", type)
               .append("offsetBegin", offsetBegin)
               .append("offsetEnd", offsetEnd)
               .append("value", value)
               .append("mask", mask)
               .append("subMatchers", subMatchers)
               .toString();
  }
}
