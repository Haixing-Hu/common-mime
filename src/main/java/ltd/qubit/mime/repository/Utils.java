////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2017 - 2024.
//    Nanjing Smart Medical Investment Operation Service Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility functions used by the implementation of the MIME-type repository.
 *
 * @author Haixing Hu
 */
class Utils {

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
