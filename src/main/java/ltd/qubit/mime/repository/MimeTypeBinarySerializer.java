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

import static ltd.qubit.commons.io.InputUtils.readList;
import static ltd.qubit.commons.io.InputUtils.readMap;
import static ltd.qubit.commons.io.InputUtils.readNullMark;
import static ltd.qubit.commons.io.InputUtils.readString;
import static ltd.qubit.commons.io.OutputUtils.writeCollection;
import static ltd.qubit.commons.io.OutputUtils.writeMap;
import static ltd.qubit.commons.io.OutputUtils.writeNullMark;
import static ltd.qubit.commons.io.OutputUtils.writeString;

/**
 * The {@link BinarySerializer} of the {@link MimeType} class.
 *
 * @author Haixing Hu
 */
@Immutable
final class MimeTypeBinarySerializer implements BinarySerializer {

  public static final MimeTypeBinarySerializer INSTANCE = new MimeTypeBinarySerializer();

  @Override
  public MimeType deserialize(final InputStream in, final boolean allowNull)
      throws IOException {
    if (readNullMark(in)) {
      if (allowNull) {
        return null;
      } else {
        throw new InvalidFormatException("Unexpected null value.");
      }
    }
    final MimeType result = new MimeType();
    result.name = readString(in, false);
    if (result.name.length() == 0) {
      throw new InvalidFormatException();
    }
    result.descriptions = readMap(String.class, String.class, in, true,
        false, false, result.descriptions);
    result.namespaceUri = readString(in, true);
    result.localName = readString(in, true);
    result.acronym = readString(in, true);
    result. expandedAcronym = readString(in, true);
    result.genericIcon = readString(in, true);
    result.aliases = readList(String.class, in, true, false, result.aliases);
    result.globs = readList(MimeGlob.class, in, true, false, result.globs);
    result.magics = readList(MimeMagic.class, in, true, false, result.magics);
    result.superTypes = readList(String.class, in, true, false, result.superTypes);
    return result;
  }

  @Override
  public void serialize(final OutputStream out, final Object obj) throws IOException {
    if (writeNullMark(out, obj)) {
      return;
    }
    final MimeType mime;
    try {
      mime = (MimeType) obj;
    } catch (final ClassCastException e) {
      throw new SerializationException(e);
    }
    writeString(out, mime.name);
    writeMap(String.class, String.class, out, mime.descriptions);
    writeString(out, mime.namespaceUri);
    writeString(out, mime.localName);
    writeString(out, mime.acronym);
    writeString(out, mime.expandedAcronym);
    writeString(out, mime.genericIcon);
    writeCollection(String.class, out, mime.aliases);
    writeCollection(MimeGlob.class, out, mime.globs);
    writeCollection(MimeMagic.class, out, mime.magics);
    writeCollection(String.class, out, mime.superTypes);
  }

}
