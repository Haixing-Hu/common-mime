////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import java.io.Serial;

/**
 * 当遇到未知MIME类型时抛出的异常。
 *
 * @author 胡海星
 */
public class UnknownMimeTypeException extends Exception {

  @Serial
  private static final long serialVersionUID = -790336086363139440L;

  private final String mimeTypeName;

  /**
   * 构造一个无参数的未知MIME类型异常。
   */
  public UnknownMimeTypeException() {
    super();
    mimeTypeName = null;
  }

  /**
   * 构造一个指定MIME类型名称的未知MIME类型异常。
   *
   * @param mimeTypeName
   *     未知的MIME类型名称。
   */
  public UnknownMimeTypeException(final String mimeTypeName) {
    super();
    this.mimeTypeName = mimeTypeName;
  }

  /**
   * 构造一个指定MIME类型名称和错误消息的未知MIME类型异常。
   *
   * @param mimeTypeName
   *     未知的MIME类型名称。
   * @param message
   *     详细错误消息。
   */
  public UnknownMimeTypeException(final String mimeTypeName, final String message) {
    super(message);
    this.mimeTypeName = mimeTypeName;
  }

  /**
   * 获取未知的MIME类型名称。
   *
   * @return 未知的MIME类型名称。
   */
  public String getMimeTypeName() {
    return mimeTypeName;
  }
}