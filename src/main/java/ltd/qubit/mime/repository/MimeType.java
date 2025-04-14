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
 * 此类的对象表示文件的MIME类型。
 *
 * @author 胡海星
 * @see <a href='http://standards.freedesktop.org/shared-mime-info-spec/shared-mime-info-spec-0.19.html'>Shared MIME-info Database</a>
 * @see <a href='http://www.freedesktop.org/wiki/Software/shared-mime-info'>shared-mime-info</a>
 * @repository
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

  /**
   * 构造一个默认的MIME类型对象。
   */
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

  /**
   * 获取此MIME类型的名称。
   *
   * @return 此MIME类型的名称。
   */
  public String getName() {
    return name;
  }

  /**
   * 获取此MIME类型的描述。
   *
   * @return 此MIME类型的描述。基于当前语言环境选择合适的描述。如果没有找到合适的描述，则返回null。
   */
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

  /**
   * 获取此MIME类型的命名空间URI。
   *
   * @return 此MIME类型的命名空间URI。
   */
  public String getNamespaceURI() {
    return namespaceUri;
  }

  /**
   * 获取此MIME类型的本地名称。
   *
   * @return 此MIME类型的本地名称。
   */
  public String getLocalName() {
    return localName;
  }

  /**
   * 获取此MIME类型的缩写。
   *
   * @return 此MIME类型的缩写。
   */
  public String getAcronym() {
    return acronym;
  }

  /**
   * 获取此MIME类型的扩展缩写。
   *
   * @return 此MIME类型的扩展缩写。
   */
  public String getExpandedAcronym() {
    return expandedAcronym;
  }

  /**
   * 获取此MIME类型的通用图标。
   *
   * @return 此MIME类型的通用图标。
   */
  public String getGenericIcon() {
    return genericIcon;
  }

  /**
   * 获取此MIME类型的超类型列表。
   *
   * @return 此MIME类型的超类型的不可修改列表。如果没有超类型，则返回空列表。
   */
  public List<String> getSuperTypes() {
    if (superTypes == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(superTypes);
    }
  }

  /**
   * 获取此MIME类型的别名列表。
   *
   * @return 此MIME类型的别名的不可修改列表。如果没有别名，则返回空列表。
   */
  public List<String> getAliases() {
    if (aliases == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(aliases);
    }
  }

  /**
   * 获取此MIME类型的Glob模式列表。
   *
   * @return 此MIME类型的Glob模式的不可修改列表。如果没有Glob模式，则返回空列表。
   */
  public List<MimeGlob> getGlobs() {
    if (globs == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(globs);
    }
  }

  /**
   * 获取此MIME类型的魔数匹配器列表。
   *
   * @return 此MIME类型的魔数匹配器的不可修改列表。如果没有魔数匹配器，则返回空列表。
   */
  public List<MimeMagic> getMagics() {
    if (magics == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(magics);
    }
  }

  /**
   * 测试指定的文件名是否与此MIME类型匹配。
   *
   * @param filename
   *     要测试的文件名。
   * @return
   *     如果指定的文件名与此MIME类型匹配，则返回true；否则返回false。
   */
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

  /**
   * 测试指定的内容缓冲区是否与此MIME类型匹配。
   *
   * @param buffer
   *     内容缓冲区。
   * @param nBytes
   *     缓冲区中要测试的字节数。
   * @return
   *     如果指定的内容缓冲区与此MIME类型匹配，则返回true；否则返回false。
   * @throws IllegalArgumentException
   *     如果nBytes大于buffer.length。
   */
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

  /**
   * 获取与指定内容缓冲区匹配的魔数匹配器。
   *
   * @param buffer
   *     内容缓冲区。
   * @param nBytes
   *     缓冲区中要测试的字节数。
   * @param bestPriority
   *     目前最佳匹配的优先级。
   * @return
   *     与指定内容缓冲区匹配的魔数匹配器。如果没有匹配，则返回null。
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return name.hashCode();
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
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