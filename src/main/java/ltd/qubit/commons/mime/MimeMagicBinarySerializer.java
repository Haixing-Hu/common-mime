////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2023.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.commons.mime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.concurrent.Immutable;

import ltd.qubit.commons.io.error.InvalidFormatException;
import ltd.qubit.commons.io.error.SerializationException;
import ltd.qubit.commons.io.serialize.BinarySerializer;

import static ltd.qubit.commons.io.InputUtils.readInt;
import static ltd.qubit.commons.io.InputUtils.readList;
import static ltd.qubit.commons.io.InputUtils.readNullMark;
import static ltd.qubit.commons.io.OutputUtils.writeCollection;
import static ltd.qubit.commons.io.OutputUtils.writeInt;
import static ltd.qubit.commons.io.OutputUtils.writeNullMark;


/**
 * The {@link BinarySerializer} of the {@link MimeMagic} class.
 *
 * @author Haixing Hu
 */
@Immutable
public final class MimeMagicBinarySerializer implements BinarySerializer {

  public static final MimeMagicBinarySerializer INSTANCE = new MimeMagicBinarySerializer();

  @Override
  public MimeMagic deserialize(final InputStream in, final boolean allowNull)
      throws IOException {
    if (readNullMark(in)) {
      if (allowNull) {
        return null;
      } else {
        throw new InvalidFormatException("Unexpected null value.");
      }
    }
    final MimeMagic result = new MimeMagic();
    result.priority = readInt(in);
    result.matchers = readList(MimeMagicMatcher.class, in, false, false,
        result.matchers);
    return result;
  }

  @Override
  public void serialize(final OutputStream out, final Object obj) throws IOException {
    if (writeNullMark(out, obj)) {
      return;
    }
    final MimeMagic magic;
    try {
      magic = (MimeMagic) obj;
    } catch (final ClassCastException e) {
      throw new SerializationException(e);
    }
    writeInt(out, magic.priority);
    writeCollection(MimeMagicMatcher.class, out, magic.matchers);
  }

}
