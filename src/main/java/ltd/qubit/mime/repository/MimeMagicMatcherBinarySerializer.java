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

import static ltd.qubit.commons.io.InputUtils.readByteArray;
import static ltd.qubit.commons.io.InputUtils.readInt;
import static ltd.qubit.commons.io.InputUtils.readList;
import static ltd.qubit.commons.io.InputUtils.readNullMark;
import static ltd.qubit.commons.io.OutputUtils.writeByteArray;
import static ltd.qubit.commons.io.OutputUtils.writeCollection;
import static ltd.qubit.commons.io.OutputUtils.writeInt;
import static ltd.qubit.commons.io.OutputUtils.writeNullMark;
import static ltd.qubit.mime.repository.MimeMagicMatcher.TYPE_NAMES;

/**
 * The {@link BinarySerializer} of the {@link MimeMagicMatcher} class.
 *
 * @author Haixing Hu
 */
@Immutable
final class MimeMagicMatcherBinarySerializer implements BinarySerializer {

  public static final MimeMagicMatcherBinarySerializer INSTANCE = new MimeMagicMatcherBinarySerializer();

  @Override
  public MimeMagicMatcher deserialize(final InputStream in,
      final boolean allowNull) throws IOException {
    if (readNullMark(in)) {
      if (allowNull) {
        return null;
      } else {
        throw new InvalidFormatException("Unexpected null value.");
      }
    }
    final MimeMagicMatcher result = new MimeMagicMatcher();
    result.type = readInt(in);
    if (result.type >= TYPE_NAMES.length) {
      throw new InvalidFormatException("Invalid type index: " + result.type);
    }
    result.offsetBegin = readInt(in);
    result.offsetEnd = readInt(in);
    if ((result.offsetBegin < 0) || (result.offsetEnd < 0)
        || (result.offsetBegin > result.offsetEnd)) {
      throw new InvalidFormatException("Invalid offset: <" + result.offsetBegin
          + ", " + result.offsetEnd + ">");
    }
    result.value = readByteArray(in, true, result.value);
    result.mask = readByteArray(in, true, result.mask);
    result.subMatchers = readList(MimeMagicMatcher.class, in,
        false, false, result.subMatchers);
    return result;
  }

  @Override
  public void serialize(final OutputStream out, final Object obj) throws IOException {
    if (writeNullMark(out, obj)) {
      return;
    }
    final MimeMagicMatcher matcher;
    try {
      matcher = (MimeMagicMatcher) obj;
    } catch (final ClassCastException e) {
      throw new SerializationException(e);
    }
    writeInt(out, matcher.type);
    writeInt(out, matcher.offsetBegin);
    writeInt(out, matcher.offsetEnd);
    writeByteArray(out, matcher.value);
    writeByteArray(out, matcher.mask);
    writeCollection(MimeMagicMatcher.class, out, matcher.subMatchers);
  }
}
