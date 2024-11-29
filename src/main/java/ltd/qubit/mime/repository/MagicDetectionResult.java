////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import java.util.LinkedList;
import java.util.List;

class MagicDetectionResult {

  int bestPriority;
  List<MimeType> list;

  MagicDetectionResult() {
    bestPriority = MimeMagic.MIN_PRIORITY - 1;
    list = new LinkedList<>();
  }

  boolean compareAdd(final MimeMagic magic, final MimeType mime) {
    if (list.isEmpty()) {
      list.add(mime);
      bestPriority = magic.getPriority();
      return true;
    } else {
      final int priority = magic.getPriority();
      if (priority > bestPriority) {
        list.clear();
        list.add(mime);
        bestPriority = priority;
        return true;
      } else if (priority == bestPriority) {
        if (!list.contains(mime)) {
          // don't add duplicated mime
          list.add(mime);
        }
        return true;
      }
    }
    return false;
  }
}
