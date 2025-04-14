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

/**
 * Glob模式检测结果类，用于存储基于文件名Glob模式匹配的MIME类型检测结果。
 *
 * @author 胡海星
 * @repository
 */
class GlobDetectionResult {

  int bestWeight;
  int bestLength;
  List<MimeType> list;

  /**
   * 构造一个默认的Glob检测结果对象。
   */
  GlobDetectionResult() {
    bestWeight = MimeGlob.MIN_WEIGHT - 1;
    bestLength = 0;
    list = new LinkedList<>();
  }

  /**
   * 比较并添加匹配结果。
   * <p>
   * 如果当前Glob的权重高于已知的最佳权重，或者权重相同但模式长度更长，
   * 则清除现有列表并添加新的MIME类型。如果权重和长度都相同，则将MIME类型
   * 添加到列表中。
   *
   * @param glob
   *     匹配的Glob模式。
   * @param mime
   *     与此Glob模式关联的MIME类型。
   */
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