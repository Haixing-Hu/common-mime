////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2023.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.commons.mime;

import java.text.ParseException;

import javax.annotation.concurrent.Immutable;

import ltd.qubit.commons.io.serialize.XmlSerializer;
import ltd.qubit.commons.text.CStringLiteral;
import ltd.qubit.commons.text.NumberFormat;
import ltd.qubit.commons.text.NumberFormatOptions;
import ltd.qubit.commons.text.xml.InvalidXmlAttributeException;
import ltd.qubit.commons.text.xml.XmlException;
import ltd.qubit.commons.text.xml.XmlSerializationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static ltd.qubit.commons.mime.MimeMagicMatcher.TYPE_BIG16;
import static ltd.qubit.commons.mime.MimeMagicMatcher.TYPE_BIG32;
import static ltd.qubit.commons.mime.MimeMagicMatcher.TYPE_BYTE;
import static ltd.qubit.commons.mime.MimeMagicMatcher.TYPE_HOST16;
import static ltd.qubit.commons.mime.MimeMagicMatcher.TYPE_HOST32;
import static ltd.qubit.commons.mime.MimeMagicMatcher.TYPE_LITTLE16;
import static ltd.qubit.commons.mime.MimeMagicMatcher.TYPE_LITTLE32;
import static ltd.qubit.commons.mime.MimeMagicMatcher.TYPE_NAMES;
import static ltd.qubit.commons.mime.MimeMagicMatcher.TYPE_STRING;
import static ltd.qubit.commons.mime.MimeMagicMatcher.TYPE_UNKNOWN;
import static ltd.qubit.commons.text.xml.DomUtils.checkNode;
import static ltd.qubit.commons.text.xml.DomUtils.getReqStringAttr;

/**
 * The {@link XmlSerializer} of the {@link MimeMagicMatcher} class.
 *
 * @author Haixing Hu
 */
@Immutable
public final class MimeMagicMatcherXmlSerializer implements XmlSerializer {

  public static final MimeMagicMatcherXmlSerializer INSTANCE = new MimeMagicMatcherXmlSerializer();

  public static final Logger LOGGER = LoggerFactory.getLogger(MimeMagicMatcherXmlSerializer.class);

  public static final String  ROOT_NODE                = "match";

  public static final String  TYPE_ATTRIBUTE           = "type";

  public static final String  MASK_ATTRIBUTE            = "mask";

  public static final String  OFFSET_ATTRIBUTE          = "offset";

  public static final String  VALUE_ATTRIBUTE           = "value";

  public static final char OFFSET_RANGE_SEPARATOR       = ':';

  @Override
  public String getRootNodeName() {
    return ROOT_NODE;
  }

  @Override
  public MimeMagicMatcher deserialize(final Element root) throws XmlException {
    checkNode(root, ROOT_NODE);
    final MimeMagicMatcher result = new MimeMagicMatcher();
    // parse the type
    parseType(root, result);
    // parse the offset
    parseOffset(root, result);
    // parse the value
    parseValue(root, result);
    // parse the optional mask
    parseMask(root, result);
    // parse the optional sub-matchers
    parseSubmatchers(root, result);
    return result;
  }

  private void parseType(final Element root, final MimeMagicMatcher result)
      throws XmlException {
    final String typeAttr = getReqStringAttr(root, TYPE_ATTRIBUTE, true, false);
    result.type = TYPE_UNKNOWN;
    for (int i = 0; i < TYPE_NAMES.length; ++i) {
      if (TYPE_NAMES[i].equals(typeAttr)) {
        result.type = i;
        break;
      }
    }
    if (result.type == TYPE_UNKNOWN) {
      throw new InvalidXmlAttributeException(ROOT_NODE, TYPE_ATTRIBUTE,
          typeAttr);
    }
  }

  private void parseOffset(final Element root, final MimeMagicMatcher result)
      throws XmlException {
    final String offsetAttr = getReqStringAttr(root, OFFSET_ATTRIBUTE, true,
        false);
    final NumberFormat nf = new NumberFormat();
    result.offsetBegin = nf.parseInt(offsetAttr);
    if (nf.fail()) {
      result.offsetBegin = - 1;
      result.offsetEnd = - 1;
    } else {
      final int index = nf.getParseIndex();
      if (index == offsetAttr.length()) {
        // the offset is not a range
        result.offsetEnd = result.offsetBegin;
      } else if (offsetAttr.charAt(index) == OFFSET_RANGE_SEPARATOR) {
        // the offset is a correct range
        result.offsetEnd = nf.parseInt(offsetAttr, index + 1);
        if (nf.fail()) {
          result.offsetBegin = - 1;
          result.offsetEnd = - 1;
        }
      } else {
        // the offset is an incorrect range
        result.offsetBegin = - 1;
        result.offsetEnd = - 1;
      }
    }
    if ((result.offsetBegin < 0) || (result.offsetEnd < 0)
        || (result.offsetBegin > result.offsetEnd)) {
      throw new InvalidXmlAttributeException(ROOT_NODE, OFFSET_ATTRIBUTE,
          offsetAttr);
    }
  }

  private void parseValue(final Element root, final MimeMagicMatcher result)
      throws XmlException {
    final String valueAttr = getReqStringAttr(root, VALUE_ATTRIBUTE, true, false);
    final NumberFormat nf = new NumberFormat();
    result.value = null;
    // stop checkstyle: MagicNumber
    switch (result.type) {
      case TYPE_BYTE: {
        final byte byteValue = nf.parseByte(valueAttr);
        if (nf.success()) {
          // store the value in a byte array
          result.value = new byte[1];
          result.value[0] = byteValue;
        } else {
          LOGGER.error("Failed to parse the 8-bit integer '{}'", valueAttr);
        }
        break;
      }
      case TYPE_HOST16:
      case TYPE_BIG16:
      case TYPE_LITTLE16: {
        final short shortValue = nf.parseShort(valueAttr);
        if (nf.success()) {
          // store the bytes of value in big endian
          result.value = new byte[2];
          result.value[0] = (byte) ((shortValue >>> 8) & 0xFF);
          result.value[1] = (byte) (shortValue & 0xFF);
        } else {
          LOGGER.error("Failed to parse the 16-bit integer '{}'", valueAttr);
        }
        break;
      }
      case TYPE_HOST32:
      case TYPE_BIG32:
      case TYPE_LITTLE32: {
        final int intValue = nf.parseInt(valueAttr);
        if (nf.success()) {
          // store the bytes of value in big endian
          result.value = new byte[4];
          result.value[0] = (byte) ((intValue >>> 24) & 0xFF);
          result.value[1] = (byte) ((intValue >>> 16) & 0xFF);
          result.value[2] = (byte) ((intValue >>> 8) & 0xFF);
          result.value[3] = (byte) (intValue & 0xFF);
        } else {
          LOGGER.error("Failed to parse the 32-bit integer '{}'", valueAttr);
        }
        break;
      }
      case TYPE_STRING:
        try {
          result.value = CStringLiteral.decode(valueAttr, 0, valueAttr.length());
        } catch (final ParseException e) {
          LOGGER.error("Failed to parse the C-style string literal '{}': {}",
              valueAttr, e.getMessage(), e);
          break; // break the switch and log an error message
        }
        break;
      default:
        assert false : "impossible value type.";
        break;
    }
    // resume checkstyle: MagicNumber
    if (result.value == null) {
      throw new InvalidXmlAttributeException(ROOT_NODE, VALUE_ATTRIBUTE, valueAttr);
    }
  }

  private void parseMask(final Element root, final MimeMagicMatcher result)
      throws XmlException {
    result.mask = null;
    if (! root.hasAttribute(MASK_ATTRIBUTE)) {
      return;
    }
    final String maskAttr = root.getAttribute(MASK_ATTRIBUTE);
    final NumberFormat nf = new NumberFormat();
    // stop checkstyle: MagicNumber
    switch (result.type) {
      case TYPE_BYTE: {
        final byte byteValue = nf.parseByte(maskAttr);
        if (nf.success()) {
          // store the value in a byte array
          result.mask = new byte[1];
          result.mask[0] = byteValue;
        }
        break;
      }
      case TYPE_HOST16:
      case TYPE_BIG16:
      case TYPE_LITTLE16: {
        final short shortValue = nf.parseShort(maskAttr);
        if (nf.success()) {
          // store the bytes of value in big endian
          result.mask = new byte[2];
          result.mask[0] = (byte) ((shortValue >>> 8) & 0xFF);
          result.mask[1] = (byte) (shortValue & 0xFF);
        }
        break;
      }
      case TYPE_HOST32:
      case TYPE_BIG32:
      case TYPE_LITTLE32: {
        final int intValue = nf.parseInt(maskAttr);
        if (nf.success()) {
          // store the bytes of value in big endian
          result.mask = new byte[4];
          result.mask[0] = (byte) ((intValue >>> 24) & 0xFF);
          result.mask[1] = (byte) ((intValue >>> 16) & 0xFF);
          result.mask[2] = (byte) ((intValue >>> 8) & 0xFF);
          result.mask[3] = (byte) (intValue & 0xFF);
        }
        break;
      }
      case TYPE_STRING:
        // Note the description of the `mask' attribute value in the
        // specification:
        //
        // The number to AND the value in the file with before comparing it
        // to `value'. Masks for numerical types can be any number, while
        // masks for strings must be in base 16, and start with 0x
        //
        // Therefore, if the `type' is string, the value of the `mask'
        // attribute is a long hex number with the "0x" prefix and
        // has the twice of the length of the "value".
        //
        if ((maskAttr.length() < 4) || ((maskAttr.length() % 2) != 0)
            || (maskAttr.charAt(0) != '0')
            || ((maskAttr.charAt(1) != 'x') && (maskAttr.charAt(1) != 'X'))) {
          // the str is not a hex number starts with "0x" or "0X",
          // then the format is invalid.
          break; // break the switch and log an error message
        }
        // since the mask should be a hex number with prefix, it could be
        // decoded into (str.length() - 2) / 2 bytes.
        result.mask = new byte[(maskAttr.length() - 2) / 2];
        for (int i = 0, j = 2; i < result.mask.length; ++i) {
          char ch = maskAttr.charAt(j++);
          int d = Character.digit(ch, 16);
          if (d < 0) {
            // meet a invalid mask, clear the newMask
            result.mask = null;
            break;
          }
          int v = (d << 4);
          ch = maskAttr.charAt(j++);
          d = Character.digit(ch, 16);
          if (d < 0) {
            // meet a invalid mask, clear the newMask
            result.mask = null;
            break;
          }
          v |= d;
          result.mask[i] = (byte) v;
        }
        break;
      default:
        assert false : "impossible value type.";
        break;
    }
    // resume checkstyle: MagicNumber
    if (result.mask == null) {
      throw new InvalidXmlAttributeException(ROOT_NODE, MASK_ATTRIBUTE,
          maskAttr);
    }
  }

  private void parseSubmatchers(final Element root,
      final MimeMagicMatcher result) throws XmlException {
    result.subMatchers.clear();
    final NodeList subNodeList = root.getChildNodes();
    final int subNodeCount = subNodeList.getLength();
    for (int i = 0; i < subNodeCount; ++i) {
      final Node subNode = subNodeList.item(i);
      if (subNode.getNodeType() == Node.ELEMENT_NODE) {
        final MimeMagicMatcher sub = deserialize((Element) subNode);
        result.subMatchers.add(sub);
      }
    }
  }

  @Override
  public Element serialize(final Document doc, final Object obj) throws XmlException {
    final MimeMagicMatcher matcher;
    try {
      matcher = (MimeMagicMatcher) obj;
    } catch (final ClassCastException e) {
      throw new XmlSerializationException(e);
    }
    final String valueStr = bytesToString(matcher.value, matcher.type);
    String maskStr  = null;
    if ((matcher.mask != null) && (matcher.mask.length > 0)) {
      maskStr = bytesToString(matcher.mask, matcher.type);
    }
    String offsetStr = null;
    if (matcher.offsetBegin == matcher.offsetEnd) {
      offsetStr = String.valueOf(matcher.offsetBegin);
    } else {
      offsetStr = String.valueOf(matcher.offsetBegin)
          + OFFSET_RANGE_SEPARATOR
          + matcher.offsetEnd;
    }
    final Element root = doc.createElement(ROOT_NODE);
    root.setAttribute(TYPE_ATTRIBUTE, TYPE_NAMES[matcher.type]);
    root.setAttribute(OFFSET_ATTRIBUTE, offsetStr);
    root.setAttribute(VALUE_ATTRIBUTE, valueStr);
    if (maskStr != null) {
      root.setAttribute(MASK_ATTRIBUTE, maskStr);
    }
    for (final MimeMagicMatcher sub : matcher.subMatchers) {
      final Element subNode = serialize(doc, sub);
      root.appendChild(subNode);
    }
    return root;
  }

  private static String bytesToString(final byte[] array, final int type) {
    final StringBuilder builder = new StringBuilder();
    final NumberFormat nf = new NumberFormat();
    final NumberFormatOptions options = nf.getOptions();
    options.setHex(true);
    options.setShowRadix(true);
    options.setUppercase(true);
    options.setUppercaseRadixPrefix(false);
    // stop checkstyle: MagicNumber
    switch (type) {
      case TYPE_STRING:
        CStringLiteral.encode(array, builder);
        break;
      case TYPE_HOST16:
      case TYPE_BIG16:
      case TYPE_LITTLE16: {
        final short theValue = (short) (((array[0] & 0xFF) << 8) | (array[1] & 0xFF));
        options.setIntPrecision(Short.SIZE / 4);
        nf.formatShort(theValue, builder);
        break;
      }
      case TYPE_HOST32:
      case TYPE_BIG32:
      case TYPE_LITTLE32: {
        // recall that value is in big endianess
        final int theValue = (((array[0] & 0xFF) << 24)
            | ((array[1] & 0xFF) << 16) | ((array[2] & 0xFF) << 8) | (array[3] & 0xFF));
        options.setIntPrecision(Integer.SIZE / 4);
        nf.formatInt(theValue, builder);
        break;
      }
      case TYPE_BYTE:
        options.setIntPrecision(Byte.SIZE / 4);
        nf.formatByte(array[0], builder);
        break;
      default:
        return "";
    }
    // resume checkstyle: MagicNumber
    return builder.toString();
  }
}
