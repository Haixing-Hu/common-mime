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
 * 魔数检测结果类，用于存储基于文件内容魔数匹配的MIME类型检测结果。
 *
 * @author 胡海星
 * @repository
 */
class MagicDetectionResult {

  int bestPriority;
  List<MimeType> list;

  /**
   * 构造一个默认的魔数检测结果对象。
   */
  MagicDetectionResult() {
    bestPriority = MimeMagic.MIN_PRIORITY - 1;
    list = new LinkedList<>();
  }

  /**
   * 比较并添加匹配结果。
   * <p>
   * 如果当前魔数的优先级高于已知的最佳优先级，则清除现有列表并添加新的
   * MIME类型。如果优先级相同，则将MIME类型添加到列表中（避免重复添加）。
   *
   * @param magic
   *     匹配的魔数。
   * @param mime
   *     与此魔数关联的MIME类型。
   * @return 如果添加成功（即优先级大于等于当前最佳优先级），则返回true；
   *     否则返回false。
   */
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