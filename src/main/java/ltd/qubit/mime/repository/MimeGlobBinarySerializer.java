////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2017 - 2024.
//    Nanjing Smart Medical Investment Operation Service Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.concurrent.Immutable;

import ltd.qubit.commons.io.error.InvalidFormatException;
import ltd.qubit.commons.io.error.SerializationException;
import ltd.qubit.commons.io.serialize.BinarySerializer;

import static ltd.qubit.commons.io.InputUtils.readBoolean;
import static ltd.qubit.commons.io.InputUtils.readInt;
import static ltd.qubit.commons.io.InputUtils.readNullMark;
import static ltd.qubit.commons.io.InputUtils.readString;
import static ltd.qubit.commons.io.OutputUtils.writeBoolean;
import static ltd.qubit.commons.io.OutputUtils.writeInt;
import static ltd.qubit.commons.io.OutputUtils.writeNullMark;
import static ltd.qubit.commons.io.OutputUtils.writeString;

/**
 * The {@link BinarySerializer} of the {@link MimeGlob} class.
 *
 * @author Haixing Hu
 */
@Immutable
final class MimeGlobBinarySerializer implements BinarySerializer {

  public static final MimeGlobBinarySerializer INSTANCE = new MimeGlobBinarySerializer();

  @Override
  public MimeGlob deserialize(final InputStream in, final boolean allowNull)
      throws IOException {
    if (readNullMark(in)) {
      if (allowNull) {
        return null;
      } else {
        throw new InvalidFormatException("Unexpected null value.");
      }
    }
    final MimeGlob result = new MimeGlob();
    result.weight = readInt(in);
    result.caseSensitive = readBoolean(in);
    result.pattern = readString(in, false);
    result.matcher = null;
    return result;
  }

  @Override
  public void serialize(final OutputStream out, final Object obj) throws IOException {
    if (writeNullMark(out, obj)) {
      return;
    }
    final MimeGlob glob;
    try {
      glob = (MimeGlob) obj;
    } catch (final ClassCastException e) {
      throw new SerializationException(e);
    }
    writeInt(out, glob.weight);
    writeBoolean(out, glob.caseSensitive);
    writeString(out, glob.pattern);
  }

}
