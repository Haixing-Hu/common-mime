////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ltd.qubit.commons.io.serialize.XmlSerializer;
import ltd.qubit.commons.lang.StringUtils;
import ltd.qubit.commons.text.xml.XmlException;
import ltd.qubit.commons.text.xml.XmlSerializationException;

import static ltd.qubit.commons.text.xml.DomUtils.appendOptStringChild;
import static ltd.qubit.commons.text.xml.DomUtils.appendSerChildren;
import static ltd.qubit.commons.text.xml.DomUtils.checkNode;
import static ltd.qubit.commons.text.xml.DomUtils.getChildren;
import static ltd.qubit.commons.text.xml.DomUtils.getOptChild;
import static ltd.qubit.commons.text.xml.DomUtils.getOptSerChildren;
import static ltd.qubit.commons.text.xml.DomUtils.getOptStringAttr;
import static ltd.qubit.commons.text.xml.DomUtils.getOptStringChild;
import static ltd.qubit.commons.text.xml.DomUtils.getReqChildren;
import static ltd.qubit.commons.text.xml.DomUtils.getReqString;
import static ltd.qubit.commons.text.xml.DomUtils.getReqStringAttr;

/**
 * The {@link XmlSerializer} of the {@link MimeType} class.
 *
 * @author Haixing Hu
 */
@Immutable
final class MimeTypeXmlSerializer implements XmlSerializer {

  public static final MimeTypeXmlSerializer INSTANCE = new MimeTypeXmlSerializer();

  public static final String  ROOT_NODE                 = "mime-type";

  public static final String  COMMENT_NODE              = "comment";

  public static final String  ROOT_XML_NODE             = "root-XML";

  public static final String  ALIAS_NODE                = "alias";

  public static final String  ACRONYM_NODE              = "acronym";

  public static final String  EXPANDED_ACRONYM_NODE     = "expanded-acronym";

  public static final String  SUB_CLASS_OF_NODE         = "sub-class-of";

  public static final String  ICON_NODE                 = "icon";

  public static final String  GENERIC_ICON_NODE         = "generic-icon";

  public static final String  TREEMAGIC_NODE            = "treemagic";

  public static final String  TYPE_ATTRIBUTE            = "type";

  public static final String  LANGUAGE_ATTRIBUTE        = "xml:lang";

  public static final String  NS_URI_ATTRIBUTE          = "namespaceURI";

  public static final String  LOCAL_NAME_ATTRIBUTE      = "localName";

  public static final String  NAME_ATTRIBUTE            = "name";

  public static final String  PATH_ATTRIBUTE            = "path";

  public static final String  MATCH_CASE_ATTRIBUTE      = "match-case";

  public static final String  EXECUTABLE_ATTRIBUTE      = "executable";

  public static final String  NON_EMPTY_ATTRIBUTE       = "non-empty";

  public static final String  MIMETYPE_ATTRIBUTE        = "mimetype";

  public static final char    OFFSET_RANGE_SEPARATOR    = ':';

  @Override
  public String getRootNodeName() {
    return ROOT_NODE;
  }

  @Override
  public MimeType deserialize(final Element root) throws XmlException {
    checkNode(root, ROOT_NODE);
    final MimeType result = new MimeType();
    result.name = getReqStringAttr(root, TYPE_ATTRIBUTE, true, false);
    // parse one or more <comment>
    final List<Element> commentNodeList = getReqChildren(root,
        COMMENT_NODE, 1, - 1, null);
    result.descriptions = new HashMap<>();
    for (final Element node : commentNodeList) {
      final String lang = getOptStringAttr(node, LANGUAGE_ATTRIBUTE, true,
          StringUtils.EMPTY);
      final String comment = getReqString(node, null, true, false);
      result.descriptions.put(lang, comment);
    }
    Element node = null;
    List<Element> nodeList = null;
    // parse the optional <acronym>
    result.acronym = getOptStringChild(root, ACRONYM_NODE, null, true, null);
    // parse the optional <expand-acronym>
    result.expandedAcronym = getOptStringChild(root, EXPANDED_ACRONYM_NODE, null,
        true, null);
    // parse the "name" attribute of the optional <generic-icon>
    result.genericIcon = null;
    node = getOptChild(root, GENERIC_ICON_NODE);
    if (node != null) {
      result.genericIcon = getOptStringAttr(node, NAME_ATTRIBUTE, true, null);
    }
    // parse the "namespaceURI" attribute and "localName" attribute of the
    // optional <root-XML>
    result.namespaceUri = null;
    result.localName = null;

    // Fixed by Haixing Hu, <root-XML> may occur one or more times
    nodeList = getChildren(root, ROOT_XML_NODE,  nodeList);
    if (nodeList != null && nodeList.size() > 0) {
      node = nodeList.get(0);
      result.namespaceUri = getOptStringAttr(node, NS_URI_ATTRIBUTE, true, null);
      result.localName = getOptStringAttr(node, LOCAL_NAME_ATTRIBUTE, true, null);
    }

    // parse the aliases
    result.aliases = null;
    nodeList = getChildren(root, ALIAS_NODE, nodeList);
    if ((nodeList != null) && (nodeList.size() > 0)) {
      result.aliases = new ArrayList<String>();
      for (final Element aliasNode : nodeList) {
        final String type = getReqStringAttr(aliasNode, TYPE_ATTRIBUTE, true,
            false);
        result.aliases.add(type);
      }
    }
    // parse the super types
    result.superTypes = null;
    nodeList = getChildren(root, SUB_CLASS_OF_NODE, nodeList);
    if ((nodeList != null) && (nodeList.size() > 0)) {
      result.superTypes = new ArrayList<String>();
      for (final Element superTypeNode : nodeList) {
        final String type = getReqStringAttr(superTypeNode, TYPE_ATTRIBUTE, true,
            false);
        result.superTypes.add(type);
      }
    }
    result.globs = getOptSerChildren(root, MimeGlob.class, false, null,
        result.globs);
    result.magics = getOptSerChildren(root, MimeMagic.class, false, null,
        result.magics);
    return result;
  }

  @Override
  public Element serialize(final Document doc, final Object obj) throws XmlException {
    final MimeType mime;
    try {
      mime = (MimeType) obj;
    } catch (final ClassCastException e) {
      throw new XmlSerializationException(e);
    }
    final Element root = doc.createElement(ROOT_NODE);
    root.setAttribute(TYPE_ATTRIBUTE, mime.name);
    if ((mime.descriptions != null) && (! mime.descriptions.isEmpty())) {
      final Set<String> langSet = mime.descriptions.keySet();
      for (final String lang : langSet) {
        final String desc = mime.descriptions.get(lang);
        final Element commentNode = doc.createElement(COMMENT_NODE);
        if (lang.length() > 0) {
          commentNode.setAttribute(LANGUAGE_ATTRIBUTE, lang);
        }
        commentNode.setTextContent(desc);
        root.appendChild(commentNode);
      }
    }
    appendOptStringChild(doc, root, ACRONYM_NODE, null, mime.acronym);
    appendOptStringChild(doc, root, EXPANDED_ACRONYM_NODE, null, mime.expandedAcronym);
    appendOptStringChild(doc, root, GENERIC_ICON_NODE, null, mime.genericIcon);
    if (mime.namespaceUri != null) {
      final Element rootXmlNode = doc.createElement(ROOT_XML_NODE);
      rootXmlNode.setAttribute(NS_URI_ATTRIBUTE, mime.namespaceUri);
      if (mime.localName != null) {
        rootXmlNode.setAttribute(LOCAL_NAME_ATTRIBUTE, mime.localName);
      }
      root.appendChild(rootXmlNode);
    }
    if ((mime.superTypes != null) && (! mime.superTypes.isEmpty())) {
      for (final String parent : mime.superTypes) {
        final Element subClassOfNode = doc.createElement(SUB_CLASS_OF_NODE);
        subClassOfNode.setAttribute(TYPE_ATTRIBUTE, parent);
        root.appendChild(subClassOfNode);
      }
    }
    appendSerChildren(doc, root, null, MimeGlob.class, mime.globs);
    appendSerChildren(doc, root, null, MimeMagic.class, mime.magics);
    if ((mime.aliases != null) && (! mime.aliases.isEmpty())) {
      for (final String alias : mime.aliases) {
        final Element aliasNode = doc.createElement(ALIAS_NODE);
        aliasNode.setAttribute(TYPE_ATTRIBUTE, alias);
        root.appendChild(aliasNode);
      }
    }
    return root;
  }

}
