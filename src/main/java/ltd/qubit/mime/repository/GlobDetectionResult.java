////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2017 - 2024.
//    Nanjing Smart Medical Investment Operation Service Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import java.util.LinkedList;
import java.util.List;

class GlobDetectionResult {

  int bestWeight;
  int bestLength;
  List<MimeType> list;

  GlobDetectionResult() {
    bestWeight = MimeGlob.MIN_WEIGHT - 1;
    bestLength = 0;
    list = new LinkedList<>();
  }

  void compareAdd(final MimeGlob glob, final MimeType mime) {
    if (list.isEmpty()) {
      list.add(mime);
      bestWeight = glob.getWeight();
      bestLength = glob.getPattern().length();
    } else {
      final int weight = glob.getWeight();
      if (weight > bestWeight) {
        list.clear();
        list.add(mime);
        bestWeight = weight;
        bestLength = glob.getPattern().length();
      } else if (weight == bestWeight) {
        final int length = glob.getPattern().length();
        if (length > bestLength) {
          list.clear();
          list.add(mime);
          bestLength = length;
        } else if (length == bestLength) {
          list.add(mime);
        }
      }
    }
  }
}
