////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2017 - 2024.
//    Nanjing Smart Medical Investment Operation Service Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import javax.annotation.concurrent.Immutable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ltd.qubit.commons.io.serialize.XmlSerializer;
import ltd.qubit.commons.text.xml.XmlException;
import ltd.qubit.commons.text.xml.XmlSerializationException;

import static ltd.qubit.commons.text.xml.DomUtils.appendSerChildren;
import static ltd.qubit.commons.text.xml.DomUtils.checkNode;
import static ltd.qubit.commons.text.xml.DomUtils.getOptIntAttr;
import static ltd.qubit.commons.text.xml.DomUtils.getReqSerChildren;
import static ltd.qubit.commons.text.xml.DomUtils.setOptIntAttr;

/**
 * The {@link XmlSerializer} of the {@link MimeMagic} class.
 *
 * @author Haixing Hu
 */
@Immutable
final class MimeMagicXmlSerializer implements XmlSerializer {

  public static final MimeMagicXmlSerializer INSTANCE = new MimeMagicXmlSerializer();

  public static final String  ROOT_NODE                 = "magic";

  public static final String  PRIORITY_ATTRIBUTE        = "priority";

  @Override
  public String getRootNodeName() {
    return ROOT_NODE;
  }

  @Override
  public MimeMagic deserialize(final Element root) throws XmlException {
    checkNode(root, ROOT_NODE);
    final MimeMagic result = new MimeMagic();
    result.priority = getOptIntAttr(root, PRIORITY_ATTRIBUTE, MimeMagic.MIN_PRIORITY,
        MimeMagic.MAX_PRIORITY, MimeMagic.DEFAULT_PRIORITY);
    result.matchers = getReqSerChildren(root, 1, - 1,
        MimeMagicMatcher.class, false, null, result.matchers);
    return result;
  }

  @Override
  public Element serialize(final Document doc, final Object obj)
      throws XmlException {
    final MimeMagic magic;
    try {
      magic = (MimeMagic) obj;
    } catch (final ClassCastException e) {
      throw new XmlSerializationException(e);
    }
    final Element root = doc.createElement(ROOT_NODE);
    setOptIntAttr(root, PRIORITY_ATTRIBUTE, magic.priority, MimeMagic.DEFAULT_PRIORITY);
    appendSerChildren(doc, root, null, MimeMagicMatcher.class, magic.matchers);
    return root;
  }

}
