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
 * 当遇到不支持的MIME类型时抛出的异常。
 *
 * @author 胡海星
 */
public class UnsupportedMimeTypeException extends Exception {

  @Serial
  private static final long serialVersionUID = 6361443562769805095L;

  private final MimeType mimeType;

  /**
   * 构造一个无参数的不支持MIME类型异常。
   */
  public UnsupportedMimeTypeException() {
    super();
    mimeType = null;
  }

  /**
   * 构造一个指定MIME类型的不支持MIME类型异常。
   *
   * @param mimeType
   *     不支持的MIME类型。
   */
  public UnsupportedMimeTypeException(final MimeType mimeType) {
    super();
    this.mimeType = mimeType;
  }

  /**
   * 构造一个指定错误消息的不支持MIME类型异常。
   *
   * @param message
   *     详细错误消息。
   */
  public UnsupportedMimeTypeException(final String message) {
    super(message);
    mimeType = null;
  }

  /**
   * 构造一个指定MIME类型和错误消息的不支持MIME类型异常。
   *
   * @param mimeType
   *     不支持的MIME类型。
   * @param message
   *     详细错误消息。
   */
  public UnsupportedMimeTypeException(final MimeType mimeType, final String message) {
    super(message);
    this.mimeType = mimeType;
  }

  /**
   * 获取不支持的MIME类型。
   *
   * @return 不支持的MIME类型。
   */
  public MimeType getMimeType() {
    return mimeType;
  }
}