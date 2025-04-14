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
import java.util.List;

/**
 * 提供MIME类型存储库实现使用的实用函数。
 *
 * @author 胡海星
 * @repository
 */
class Utils {

  /**
   * 判断指定的模式是否为扩展名模式（形如"*.xxx"）。
   *
   * @param pattern
   *     要检查的模式。
   * @return 如果指定的模式是扩展名模式，则返回true；否则返回false。
   */
  static boolean isExtensionPattern(final String pattern) {
    if ((pattern.length() > 2) && (pattern.charAt(0) == '*') && (pattern.charAt(1) == '.')) {
      for (int i = 2; i < pattern.length(); ++i) {
        final char ch = pattern.charAt(i);
        switch (ch) {
          case '*':
          case '?':
          case '{':
          case '}':
          case '!':
          case '[':
          case ']':
          case '^':
            // the pattern contains special glob characters.
            return false;
          default:
            break; // break the switch
        }
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * 判断指定的模式是否为字面量模式（不包含任何特殊glob字符）。
   *
   * @param pattern
   *     要检查的模式。
   * @return 如果指定的模式是字面量模式，则返回true；否则返回false。
   */
  static boolean isLiteralPattern(final String pattern) {
    for (int i = 0; i < pattern.length(); ++i) {
      final char ch = pattern.charAt(i);
      switch (ch) {
        case '*':
        case '?':
        case '{':
        case '}':
        case '!':
        case '[':
        case ']':
        case '^':
          // the pattern contains special glob characters.
          return false;
        default:
          break; // break the switch
      }
    }
    return true;
  }

  /**
   * 通过魔数检测MIME类型。
   *
   * @param buffer
   *     包含文件内容的缓冲区。
   * @param nBytes
   *     缓冲区中要检测的字节数。
   * @param list
   *     要检测的MIME类型列表。
   * @return 与指定内容匹配的MIME类型列表，按魔数匹配器的优先级降序排列；
   *     如果没有找到匹配项，则返回null。
   */
  static List<MimeType> detectByMagic(final byte[] buffer, final int nBytes,
      final List<MimeType> list) {
    final MagicDetectionResult result = new MagicDetectionResult();
    for (final MimeType mime : list) {
      final List<MimeMagic> magicList = mime.getMagics();
      if (magicList != null) {
        for (final MimeMagic magic : magicList) {
          if ((magic.getPriority() >= result.bestPriority)
              && magic.matches(buffer, nBytes)) {
            result.compareAdd(magic, mime);
          }
        }
      }
    }
    if (result.list.isEmpty()) {
      return null;
    } else {
      return result.list;
    }
  }

  /**
   * 通过魔数检查MIME类型列表。
   *
   * @param buffer
   *     包含文件内容的缓冲区。
   * @param nBytes
   *     缓冲区中要检测的字节数。
   * @param list
   *     要检查的MIME类型列表。
   * @return 与指定内容匹配的MIME类型列表，按魔数匹配器的优先级降序排列；
   *     如果没有找到匹配项，则返回null。
   */
  static List<MimeType> checkByMagic(final byte[] buffer, final int nBytes,
      final List<MimeType> list) {
    final List<MimeType> result = new ArrayList<>();
    int bestPriority = MimeMagic.MIN_PRIORITY - 1;
    for (final MimeType mime : list) {
      final MimeMagic magic = mime.getMatchedMagic(buffer, nBytes, bestPriority);
      if (magic != null) {
        final int priority = magic.getPriority();
        if (priority > bestPriority) {
          result.clear();
          result.add(mime);
          bestPriority = priority;
        } else {
          result.add(mime);
        }
      }
    }
    if (result.isEmpty()) {
      return null;
    } else {
      return result;
    }
  }
}