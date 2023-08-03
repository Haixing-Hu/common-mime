////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2023.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.commons.mime;

/**
 * Thrown to indicate an unsupported MIME-type.
 *
 * @author Haixing Hu
 */
public class UnsupportedMimeTypeException extends Exception {
  private static final long serialVersionUID = 6361443562769805095L;

  private final MimeType mimeType;

  public UnsupportedMimeTypeException() {
    mimeType = null;
  }

  public UnsupportedMimeTypeException(final MimeType mimeType) {
    this.mimeType = mimeType;
  }

  public UnsupportedMimeTypeException(final String message) {
    super(message);
    mimeType = null;
  }

  public UnsupportedMimeTypeException(final MimeType mimeType, final String message) {
    super(message);
    this.mimeType = mimeType;
  }

  public MimeType getMimeType() {
    return mimeType;
  }
}
