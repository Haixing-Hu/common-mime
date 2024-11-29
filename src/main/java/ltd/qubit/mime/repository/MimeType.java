////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import ltd.qubit.commons.io.serialize.BinarySerialization;
import ltd.qubit.commons.io.serialize.XmlSerialization;
import ltd.qubit.commons.lang.Assignment;
import ltd.qubit.commons.lang.CloneableEx;
import ltd.qubit.commons.lang.StringUtils;
import ltd.qubit.commons.text.tostring.ToStringBuilder;

/**
 * The object of this class represents the MIME type of a file.
 *
 * @author Haixing Hu
 * @see <a href='http://standards.freedesktop.org/shared-mime-info-spec/shared-mime-info-spec-0.19.html'>Shared MIME-info Database</a>
 * @see <a href='http://www.freedesktop.org/wiki/Software/shared-mime-info'>shared-mime-info</a>
 */
@NotThreadSafe
public final class MimeType implements Serializable, CloneableEx<MimeType> {

  private static final long   serialVersionUID          = -6109152808063707682L;

  static {
    BinarySerialization.register(MimeType.class, MimeTypeBinarySerializer.INSTANCE);
    XmlSerialization.register(MimeType.class, MimeTypeXmlSerializer.INSTANCE);
  }

  String                name;
  Map<String, String>   descriptions;
  String                namespaceUri;
  String                localName;
  String                acronym;
  String                expandedAcronym;
  String                genericIcon;
  List<String>          aliases;
  List<MimeGlob>        globs;
  List<MimeMagic>       magics;
  List<String>          superTypes;

  public MimeType() {
    name = StringUtils.EMPTY;
    descriptions = null;
    namespaceUri = null;
    localName = null;
    acronym = null;
    expandedAcronym = null;
    genericIcon = null;
    aliases = null;
    globs = null;
    magics = null;
    superTypes = null;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    if ((descriptions == null) || descriptions.isEmpty()) {
      return null;
    }
    // try to get the description for the current locale.
    final Locale loc = Locale.getDefault();
    String desc = descriptions.get(loc.toString());
    if (desc != null) {
      return desc;
    }
    desc = descriptions.get(loc.getLanguage());
    if (desc != null) {
      return desc;
    }
    // try to get the description for default languages.
    desc = descriptions.get(StringUtils.EMPTY);
    if (desc != null) {
      return desc;
    }
    desc = descriptions.get("en");
    if (desc != null) {
      return desc;
    }
    desc = descriptions.get("en_US");
    if (desc != null) {
      return desc;
    }
    desc = descriptions.get("en_GB");
    if (desc != null) {
      return desc;
    }
    // if non of above language can be found,
    // just return the first description.
    final Set<String> langSet = descriptions.keySet();
    for (final String lang : langSet) {
      return descriptions.get(lang);
    }
    return null;
  }

  public String getNamespaceURI() {
    return namespaceUri;
  }

  public String getLocalName() {
    return localName;
  }

  public String getAcronym() {
    return acronym;
  }

  public String getExpandedAcronym() {
    return expandedAcronym;
  }

  public String getGenericIcon() {
    return genericIcon;
  }

  public List<String> getSuperTypes() {
    if (superTypes == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(superTypes);
    }
  }

  public List<String> getAliases() {
    if (aliases == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(aliases);
    }
  }

  public List<MimeGlob> getGlobs() {
    if (globs == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(globs);
    }
  }

  public List<MimeMagic> getMagics() {
    if (magics == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(magics);
    }
  }

  public boolean matches(final String filename) {
    if ((globs == null) || globs.isEmpty()) {
      return false;
    } else {
      for (final MimeGlob glob : globs) {
        if (glob.matches(filename)) {
          return true;
        }
      }
      return false;
    }
  }

  public boolean matches(final byte[] buffer, final int nBytes) {
    if (nBytes > buffer.length) {
      throw new IllegalArgumentException();
    }

    if ((magics != null) && (! magics.isEmpty())) {
      for (final MimeMagic magic : magics) {
        if (magic.matches(buffer, nBytes)) {
          return true;
        }
      }
    }

    if ((superTypes == null) || superTypes.isEmpty()) {
      return false;
    }

    final MimeRepository repository = MimeRepository.getInstance();
    for (final String parentName : superTypes) {
      final MimeType parent = repository.get(parentName);
      if ((parent != null) && parent.matches(buffer, nBytes)) {
        return true;
      }
    }
    return false;
  }

  MimeMagic getMatchedMagic(final byte[] buffer, final int nBytes,
      int bestPriority) {
    if ((magics != null) && (! magics.isEmpty())) {
      MimeMagic result = null;
      for (final MimeMagic magic : magics) {
        final int priority = magic.getPriority();
        if ((priority >= bestPriority) && magic.matches(buffer, nBytes)) {
          result = magic;
          bestPriority = priority;
        }
      }
      return result;
    } else if ((superTypes != null) && (! superTypes.isEmpty())) {
      MimeMagic result = null;
      final MimeRepository repository = MimeRepository.getInstance();
      for (final String parentName : superTypes) {
        final MimeType parent = repository.get(parentName);
        if (parent != null) {
          final MimeMagic magic = parent.getMatchedMagic(buffer, nBytes, bestPriority);
          if (magic != null) {
            result = magic;
            bestPriority = magic.getPriority();
          }
        }
      }
      return result;
    } else {
      return null;
    }
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof MimeType)) {
      return false;
    }
    final MimeType other = (MimeType) obj;
    return name.equals(other.name);
  }

  @Override
  public MimeType cloneEx() {
    final MimeType cloned = new MimeType();
    cloned.name = name;
    cloned.descriptions = Assignment.cloneMap(descriptions);
    cloned.namespaceUri = namespaceUri;
    cloned.localName = localName;
    cloned.acronym = acronym;
    cloned.expandedAcronym = expandedAcronym;
    cloned.genericIcon = genericIcon;
    cloned.aliases = Assignment.cloneList(aliases);
    cloned.globs = Assignment.deepClone(globs);
    cloned.magics = Assignment.deepClone(magics);
    cloned.superTypes = Assignment.cloneList(superTypes);
    return cloned;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
               .append("name", name)
               .append("descriptions", descriptions)
               .append("namespaceUri", namespaceUri)
               .append("localName", localName)
               .append("acronym", acronym)
               .append("expandedAcronym", expandedAcronym)
               .append("genericIcon", genericIcon)
               .append("aliases", aliases)
               .append("globs", globs)
               .append("magics", magics)
               .append("superTypes", superTypes)
               .toString();
  }
}
