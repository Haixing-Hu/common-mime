////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2017 - 2024.
//    Nanjing Smart Medical Investment Operation Service Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

/**
 * Thrown to indicate an unknown MIME-type.
 *
 * @author Haixing Hu
 */
public class UnknownMimeTypeException extends Exception {

  private static final long serialVersionUID = -790336086363139440L;

  private final String mimeTypeName;

  public UnknownMimeTypeException() {
    super();
    mimeTypeName = null;
  }

  public UnknownMimeTypeException(final String mimeTypeName) {
    super();
    this.mimeTypeName = mimeTypeName;
  }

  public UnknownMimeTypeException(final String mimeTypeName, final String message) {
    super(message);
    this.mimeTypeName = mimeTypeName;
  }

  public String getMimeTypeName() {
    return mimeTypeName;
  }
}
