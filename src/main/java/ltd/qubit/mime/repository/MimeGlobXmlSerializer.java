////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import javax.annotation.concurrent.Immutable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ltd.qubit.commons.io.serialize.XmlSerializer;
import ltd.qubit.commons.text.xml.InvalidXmlNodeContentException;
import ltd.qubit.commons.text.xml.XmlException;
import ltd.qubit.commons.text.xml.XmlSerializationException;

import static ltd.qubit.commons.text.xml.DomUtils.checkNode;
import static ltd.qubit.commons.text.xml.DomUtils.getOptBooleanAttr;
import static ltd.qubit.commons.text.xml.DomUtils.getOptIntAttr;
import static ltd.qubit.commons.text.xml.DomUtils.getReqStringAttr;
import static ltd.qubit.commons.text.xml.DomUtils.setOptBooleanAttr;
import static ltd.qubit.commons.text.xml.DomUtils.setOptIntAttr;
import static ltd.qubit.mime.repository.MimeGlob.DEFAULT_CASE_SENSITIVE;
import static ltd.qubit.mime.repository.MimeGlob.DEFAULT_WEIGHT;
import static ltd.qubit.mime.repository.MimeGlob.MAX_WEIGHT;
import static ltd.qubit.mime.repository.MimeGlob.MIN_WEIGHT;

/**
 * The {@link XmlSerializer} of the {@link MimeGlob} class.
 *
 * @author Haixing Hu
 */
@Immutable
final class MimeGlobXmlSerializer implements XmlSerializer {

  public static final MimeGlobXmlSerializer INSTANCE = new MimeGlobXmlSerializer();

  public static final String  ROOT_NODE                 = "glob";

  public static final String  PATTERN_ATTRIBUTE         = "pattern";

  public static final String  WEIGHT_ATTRIBUTE          = "weight";

  public static final String  CASE_SENSITIVE_ATTRIBUTE  = "case-sensitive";

  @Override
  public String getRootNodeName() {
    return ROOT_NODE;
  }

  @Override
  public MimeGlob deserialize(final Element root) throws XmlException {
    checkNode(root, ROOT_NODE);
    if (root.hasChildNodes()) {
      throw new InvalidXmlNodeContentException(ROOT_NODE, root.getTextContent());
    }
    final MimeGlob result = new MimeGlob();
    result.weight = getOptIntAttr(root, WEIGHT_ATTRIBUTE, MIN_WEIGHT,
        MAX_WEIGHT, DEFAULT_WEIGHT);
    result.caseSensitive = getOptBooleanAttr(root, CASE_SENSITIVE_ATTRIBUTE,
        DEFAULT_CASE_SENSITIVE);
    result.pattern = getReqStringAttr(root, PATTERN_ATTRIBUTE, true, false);
    return result;
  }

  @Override
  public Element serialize(final Document doc, final Object obj)
      throws XmlException {
    final MimeGlob glob;
    try {
      glob = (MimeGlob) obj;
    } catch (final ClassCastException e) {
      throw new XmlSerializationException(e);
    }
    final Element root = doc.createElement(ROOT_NODE);
    setOptIntAttr(root, WEIGHT_ATTRIBUTE, glob.weight, DEFAULT_WEIGHT);
    setOptBooleanAttr(root, CASE_SENSITIVE_ATTRIBUTE, glob.caseSensitive,
        DEFAULT_CASE_SENSITIVE);
    root.setAttribute(PATTERN_ATTRIBUTE, glob.pattern);
    return root;
  }

}
