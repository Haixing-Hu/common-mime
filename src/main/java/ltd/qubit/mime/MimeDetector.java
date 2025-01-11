////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import ltd.qubit.commons.io.FilenameUtils;

/**
 * The interface of MIME-type detectors.
 *
 * @author Haixing Hu
 */
@Immutable
public interface MimeDetector {

  /**
   * Gets whether this detector will always check the file magic by default.
   *
   * @return
   *     whether this detector will always check the file magic by default.
   */
  boolean isAlwaysCheckMagicByDefault();

  /**
   * Sets whether this detector will always check the file magic by default.
   *
   * @param alwaysCheckMagicByDefault
   *     whether this detector will always check the file magic by default.
   */
  void setAlwaysCheckMagicByDefault(boolean alwaysCheckMagicByDefault);

  /**
   * Detects the MIME-type of a file only by its filename extension.
   *
   * @param file
   *     the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @see #detectByFilename(Path)
   * @see #detectByFilename(String)
   * @see #detectByFilename(URL)
   * @see #detectByFilename(URI)
   */
  @Nullable
  String detectByFilename(@Nonnull final File file);

  /**
   * Detects the MIME-type of a file only by its filename extension.
   *
   * @param path
   *     the path of the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @see #detectByFilename(File)
   * @see #detectByFilename(String)
   * @see #detectByFilename(URL)
   * @see #detectByFilename(URI)
   */
  @Nullable
  String detectByFilename(@Nonnull final Path path);

  /**
   * Detects the MIME-type of a file only by its filename extension.
   *
   * @param path
   *     the path of the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @see #detectByFilename(File)
   * @see #detectByFilename(Path)
   * @see #detectByFilename(URL)
   * @see #detectByFilename(URI)
   */
  @Nullable
  String detectByFilename(@Nonnull final String path);

  /**
   * Detects the MIME-type of a file only by its filename extension.
   *
   * @param url
   *     the URL of the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @see #detectByFilename(File)
   * @see #detectByFilename(Path)
   * @see #detectByFilename(String)
   * @see #detectByFilename(URI)
   */
  @Nullable
  String detectByFilename(@Nonnull final URL url);

  /**
   * Detects the MIME-type of a file only by its filename extension.
   *
   * @param uri
   *     the URI of the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @see #detectByFilename(File)
   * @see #detectByFilename(Path)
   * @see #detectByFilename(String)
   * @see #detectByFilename(URL)
   */
  @Nullable
  String detectByFilename(@Nonnull final URI uri);

  /**
   * Detects the MIME-type of a file only by its content.
   *
   * @param file
   *     the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detectByContent(Path)
   * @see #detectByContent(InputStream)
   * @see #detectByContent(byte[])
   * @see #detectByContent(URL)
   * @see #detectByContent(URI)
   */
  @Nullable
  String detectByContent(@Nonnull final File file) throws IOException;

  /**
   * Detects the MIME-type of a file only by its content.
   *
   * @param path
   *     the path of the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detectByContent(File)
   * @see #detectByContent(InputStream)
   * @see #detectByContent(byte[])
   * @see #detectByContent(URL)
   * @see #detectByContent(URI)
   */
  @Nullable
  String detectByContent(@Nonnull final Path path) throws IOException;

  /**
   * Detects the MIME-type of a file only by its content.
   * <p>
   * <b>NOTE:</b> This function does NOT close the input stream.
   *
   * @param input
   *     the input stream of the file to be detected. Note that this function
   *     does not close the input stream after use.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detectByContent(File)
   * @see #detectByContent(Path)
   * @see #detectByContent(byte[])
   * @see #detectByContent(URL)
   * @see #detectByContent(URI)
   */
  @Nullable
  String detectByContent(@Nonnull final InputStream input) throws IOException;

  /**
   * Detects the MIME-type of a file only by its content.
   *
   * @param url
   *     the URL of the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detectByContent(File)
   * @see #detectByContent(Path)
   * @see #detectByContent(InputStream)
   * @see #detectByContent(byte[])
   * @see #detectByContent(URI)
   */
  @Nullable
  String detectByContent(@Nonnull final URL url) throws IOException;

  /**
   * Detects the MIME-type of a file only by its content.
   *
   * @param uri
   *     the URI of the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detectByContent(File)
   * @see #detectByContent(Path)
   * @see #detectByContent(InputStream)
   * @see #detectByContent(byte[])
   * @see #detectByContent(URL)
   */
  @Nullable
  String detectByContent(@Nonnull final URI uri) throws IOException;

  /**
   * Detects the MIME-type of a file only by its content.
   * <p>
   * <b>NOTE:</b> This function does NOT close the input stream.
   *
   * @param content
   *     the bytes of the content of the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @see #detectByContent(File)
   * @see #detectByContent(Path)
   * @see #detectByContent(InputStream)
   * @see #detectByContent(URL)
   * @see #detectByContent(URI)
   */
  @Nullable
  String detectByContent(@Nonnull final byte[] content);

  /**
   * Detects the MIME-type of a file.
   * <p>
   * This function has the same effect as calling
   * <code>detect(file, null, isAlwaysCheckMagic())</code>.
   *
   * @param file
   *     the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(File, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final File file) throws IOException {
    return detect(file, null, isAlwaysCheckMagicByDefault());
  }

  /**
   * Detects the MIME-type of a file.
   * <p>
   * This function has the same effect as calling
   * <code>detect(file, filename, isAlwaysCheckMagic())</code>.
   *
   * @param file
   *     the file to be detected.
   * @param filename
   *     the optional filename of the file to be detected; or {@code null} if
   *     the filename should be extracted from the {@code file} argument.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(File, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final File file, @Nullable final String filename)
      throws IOException {
    return detect(file, filename, isAlwaysCheckMagicByDefault());
  }

  /**
   * Detects the MIME-type of a file.
   * <p>
   * This function use the following smart strategy to detect the MIME-type:
   * <ul>
   * <li>It firstly try to detect the possible MIME-types from the filename
   * extension.</li>
   * <li>If there is one and only one candidate MIME-type detected from the
   * filename extension, and the argument {@code alwaysCheckMagic} is
   * {@code true}, the function returns that MIME-type.</li>
   * <li>Otherwise the function will try to detect the possible MIME-types from
   * the file content.</li>
   * <li>If there is no candidate MIME-type detected from the filename extension,
   * nor from the file content, the function returns {@code null}.</li>
   * <li>If there is no candidate MIME-type detected from the filename extension,
   * but there is one or more candidate MIME-types detected from the file content,
   * the function returns the first candidate MIME-type detected from the file
   * content.</li>
   * <li>If there is any common MIME-type detected <b>both</b> from the filename
   * extension, and from the file content, the function returns the first common
   * MIME-type.</li>
   * <li>Otherwise, the function returns the first candidate MIME-type detected
   * from the filename content.</li>
   * </ul>
   *
   * @param file
   *     the file to be detected.
   * @param filename
   *     the optional filename of the file to be detected; or {@code null} if
   *     the filename should be extracted from the {@code file} argument.
   * @param alwaysCheckMagic
   *     whether to always check the magic number of the file, even if the
   *     filename extension is recognized.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(Path, String, boolean)
   * @see #detect(URL, String, boolean)
   * @see #detect(URI, String, boolean)
   * @see #detect(InputStream, String, boolean)
   * @see #detect(byte[], String, boolean)
   */
  @Nullable
  String detect(@Nonnull File file, @Nullable String filename, boolean alwaysCheckMagic)
      throws IOException;

  /**
   * Detects the MIME-type of a file.
   * <p>
   * This function has the same effect as calling
   * <code>detect(path, null, false)</code>.
   *
   * @param path
   *     the path of the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(Path, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final Path path) throws IOException {
    return detect(path, null, false);
  }

  /**
   * Detects the MIME-type of a file.
   * <p>
   * This function has the same effect as calling
   * <code>detect(path, filename, isAlwaysCheckMagic())</code>.
   *
   * @param path
   *     the path of the file to be detected.
   * @param filename
   *     the optional filename of the file to be detected; or {@code null} if
   *     the filename should be extracted from the {@code path} argument.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(Path, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final Path path, @Nullable final String filename)
      throws IOException {
    return detect(path, filename, isAlwaysCheckMagicByDefault());
  }

  /**
   * Detects the MIME-type of a file.
   * <p>
   * This function use the following smart strategy to detect the MIME-type:
   * <ul>
   * <li>It firstly try to detect the possible MIME-types from the filename
   * extension.</li>
   * <li>If there is one and only one candidate MIME-type detected from the
   * filename extension, and the argument {@code alwaysCheckMagic} is
   * {@code true}, the function returns that MIME-type.</li>
   * <li>Otherwise the function will try to detect the possible MIME-types from
   * the file content.</li>
   * <li>If there is no candidate MIME-type detected from the filename extension,
   * nor from the file content, the function returns {@code null}.</li>
   * <li>If there is no candidate MIME-type detected from the filename extension,
   * but there is one or more candidate MIME-types detected from the file content,
   * the function returns the first candidate MIME-type detected from the file
   * content.</li>
   * <li>If there is any common MIME-type detected <b>both</b> from the filename
   * extension, and from the file content, the function returns the first common
   * MIME-type.</li>
   * <li>Otherwise, the function returns the first candidate MIME-type detected
   * from the filename content.</li>
   * </ul>
   *
   * @param path
   *     the path of the file to be detected.
   * @param filename
   *     the optional filename of the file to be detected; or {@code null} if
   *     the filename should be extracted from the {@code path} argument.
   * @param alwaysCheckMagic
   *     whether to always check the magic number of the file, even if the
   *     filename extension is recognized.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(File, String, boolean)
   * @see #detect(URL, String, boolean)
   * @see #detect(URI, String, boolean)
   * @see #detect(InputStream, String, boolean)
   * @see #detect(byte[], String, boolean)
   */
  @Nullable
  String detect(@Nonnull Path path, @Nullable String filename, boolean alwaysCheckMagic)
      throws IOException;

  /**
   * Detects the MIME-type of a file.
   * <p>
   * This function has the same effect as calling
   * <code>detect(url, null, isAlwaysCheckMagic())</code>.
   *
   * @param url
   *     the URL of the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(URL, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final URL url) throws IOException {
    return detect(url, null, isAlwaysCheckMagicByDefault());
  }

  /**
   * Detects the MIME-type of a file.
   * <p>
   * This function has the same effect as calling
   * <code>detect(url, filename, isAlwaysCheckMagic())</code>.
   *
   * @param url
   *     the URL of the file to be detected.
   * @param filename
   *     the optional filename of the file to be detected; or {@code null} if
   *     the filename should be extracted from the {@code url} argument.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(URL, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final URL url, @Nullable final String filename)
      throws IOException {
    return detect(url, filename, isAlwaysCheckMagicByDefault());
  }

  /**
   * Detects the MIME-type of a file.
   * <p>
   * This function use the following smart strategy to detect the MIME-type:
   * <ul>
   * <li>It firstly try to detect the possible MIME-types from the filename
   * extension.</li>
   * <li>If there is one and only one candidate MIME-type detected from the
   * filename extension, and the argument {@code alwaysCheckMagic} is
   * {@code true}, the function returns that MIME-type.</li>
   * <li>Otherwise the function will try to detect the possible MIME-types from
   * the file content.</li>
   * <li>If there is no candidate MIME-type detected from the filename extension,
   * nor from the file content, the function returns {@code null}.</li>
   * <li>If there is no candidate MIME-type detected from the filename extension,
   * but there is one or more candidate MIME-types detected from the file content,
   * the function returns the first candidate MIME-type detected from the file
   * content.</li>
   * <li>If there is any common MIME-type detected <b>both</b> from the filename
   * extension, and from the file content, the function returns the first common
   * MIME-type.</li>
   * <li>Otherwise, the function returns the first candidate MIME-type detected
   * from the filename content.</li>
   * </ul>
   *
   * @param url
   *     the URL of the file to be detected.
   * @param filename
   *     the optional filename of the file to be detected; or {@code null} if
   *     the filename should be extracted from the {@code url} argument.
   * @param alwaysCheckMagic
   *     whether to always check the magic number of the file, even if the
   *     filename extension is recognized.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(File, String, boolean)
   * @see #detect(Path, String, boolean)
   * @see #detect(URI, String, boolean)
   * @see #detect(InputStream, String, boolean)
   * @see #detect(byte[], String, boolean)
   */
  @Nullable
  String detect(@Nonnull URL url, @Nullable String filename, boolean alwaysCheckMagic)
      throws IOException;

  /**
   * Detects the MIME-type of a file.
   * <p>
   * This function has the same effect as calling
   * <code>detect(uri, null, isAlwaysCheckMagic())</code>.
   *
   * @param uri
   *     the URI of the file to be detected.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(URI, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final URI uri) throws IOException {
    return detect(uri, null, isAlwaysCheckMagicByDefault());
  }

  /**
   * Detects the MIME-type of a file.
   * <p>
   * This function has the same effect as calling
   * <code>detect(uri, filename, isAlwaysCheckMagic())</code>.
   *
   * @param uri
   *     the URI of the file to be detected.
   * @param filename
   *     the optional filename of the file to be detected; or {@code null} if
   *     the filename should be extracted from the {@code uri} argument.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(URI, String, boolean)
   */
  @Nullable
  default String detect(@Nonnull final URI uri, @Nullable final String filename)
      throws IOException {
    return detect(uri, filename, isAlwaysCheckMagicByDefault());
  }

  /**
   * Detects the MIME-type of a file.
   * <p>
   * This function use the following smart strategy to detect the MIME-type:
   * <ul>
   * <li>It firstly try to detect the possible MIME-types from the filename
   * extension.</li>
   * <li>If there is one and only one candidate MIME-type detected from the
   * filename extension, and the argument {@code alwaysCheckMagic} is
   * {@code true}, the function returns that MIME-type.</li>
   * <li>Otherwise the function will try to detect the possible MIME-types from
   * the file content.</li>
   * <li>If there is no candidate MIME-type detected from the filename extension,
   * nor from the file content, the function returns {@code null}.</li>
   * <li>If there is no candidate MIME-type detected from the filename extension,
   * but there is one or more candidate MIME-types detected from the file content,
   * the function returns the first candidate MIME-type detected from the file
   * content.</li>
   * <li>If there is any common MIME-type detected <b>both</b> from the filename
   * extension, and from the file content, the function returns the first common
   * MIME-type.</li>
   * <li>Otherwise, the function returns the first candidate MIME-type detected
   * from the filename content.</li>
   * </ul>
   *
   * @param uri
   *     the URI of the file to be detected.
   * @param filename
   *     the optional filename of the file to be detected; or {@code null} if
   *     the filename should be extracted from the {@code uri} argument.
   * @param alwaysCheckMagic
   *     whether to always check the magic number of the file, even if the
   *     filename extension is recognized.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(File, String, boolean)
   * @see #detect(Path, String, boolean)
   * @see #detect(URL, String, boolean)
   * @see #detect(InputStream, String, boolean)
   * @see #detect(byte[], String, boolean)
   */
  @Nullable
  String detect(@Nonnull URI uri, @Nullable String filename, boolean alwaysCheckMagic)
      throws IOException;

  /**
   * Detects the MIME-type of an input stream.
   * <p>
   * This function has the same effect as calling
   * <code>detect(input, filename, isAlwaysCheckMagic())</code>.
   *
   * @param input
   *     the input stream to be detected.
   * @param filename
   *     the optional filename of the file to be detected; or {@code null} if
   *     the filename is unknown. If the filename is provided, the function will
   *     try to guess the MIME-type from the filename extension. Note that this
   *     argument must be the filename without any path component. Use
   *     {@link FilenameUtils#getFilename(File)} or
   *     {@link FilenameUtils#getFilename(Path)} to extract the filename from
   *     a {@link File} or a {@link Path}.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(InputStream, String, boolean)
   * @see FilenameUtils#getFilename(File)
   * @see FilenameUtils#getFilename(Path)
   */
  @Nullable
  default String detect(@Nonnull final InputStream input, @Nullable final String filename)
      throws IOException {
    return detect(input, filename, isAlwaysCheckMagicByDefault());
  }

  /**
   * Detects the MIME-type of an input stream.
   * <p>
   * <b>NOTE:</b> This function does NOT close the input stream.
   * <p>
   * This function use the following smart strategy to detect the MIME-type:
   * <ul>
   * <li>It firstly try to detect the possible MIME-types from the filename
   * extension.</li>
   * <li>If there is one and only one candidate MIME-type detected from the
   * filename extension, and the argument {@code alwaysCheckMagic} is
   * {@code true}, the function returns that MIME-type.</li>
   * <li>Otherwise the function will try to detect the possible MIME-types from
   * the file content.</li>
   * <li>If there is no candidate MIME-type detected from the filename extension,
   * nor from the file content, the function returns {@code null}.</li>
   * <li>If there is no candidate MIME-type detected from the filename extension,
   * but there is one or more candidate MIME-types detected from the file content,
   * the function returns the first candidate MIME-type detected from the file
   * content.</li>
   * <li>If there is any common MIME-type detected <b>both</b> from the filename
   * extension, and from the file content, the function returns the first common
   * MIME-type.</li>
   * <li>Otherwise, the function returns the first candidate MIME-type detected
   * from the filename content.</li>
   * </ul>
   *
   * @param input
   *     the input stream to be detected.
   * @param filename
   *     the optional filename of the file to be detected; or {@code null} if
   *     the filename is unknown. If the filename is provided, the function will
   *     try to guess the MIME-type from the filename extension. Note that this
   *     argument must be the filename without any path component. Use
   *     {@link FilenameUtils#getFilename(File)} or
   *     {@link FilenameUtils#getFilename(Path)} to extract the filename from
   *     a {@link File} or a {@link Path}.
   * @param alwaysCheckMagic
   *     whether to always check the magic number of the file, even if the
   *     filename extension is recognized.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   * @see #detect(File, String, boolean)
   * @see #detect(Path, String, boolean)
   * @see #detect(URL, String, boolean)
   * @see #detect(URI, String, boolean)
   * @see #detect(byte[], String, boolean)
   * @see #detect(InputStream, String)
   * @see FilenameUtils#getFilename(File)
   * @see FilenameUtils#getFilename(Path)
   */
  @Nullable
  String detect(@Nonnull InputStream input, @Nullable String filename,
      boolean alwaysCheckMagic) throws IOException;

  /**
   * Detects the MIME-type of a file from its filename and content.
   * <p>
   * This function has the same effect as calling
   * <code>detect(content, filename, isAlwaysCheckMagic())</code>.
   *
   * @param content
   *     the bytes of the content of the file to be detected.
   * @param filename
   *     the optional filename of the file to be detected; or {@code null} if
   *     the filename is unknown. If the filename is provided, the function will
   *     try to guess the MIME-type from the filename extension. Note that this
   *     argument must be the filename without any path component. Use
   *     {@link FilenameUtils#getFilename(File)} or
   *     {@link FilenameUtils#getFilename(Path)} to extract the filename from
   *     a {@link File} or a {@link Path}.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @see #detect(byte[], String, boolean)
   * @see FilenameUtils#getFilename(File)
   * @see FilenameUtils#getFilename(Path)
   */
  @Nullable
  default String detect(@Nonnull final byte[] content, @Nullable final String filename) {
    return detect(content, filename, isAlwaysCheckMagicByDefault());
  }

  /**
   * Detects the MIME-type of a file from its filename and content.
   * <p>
   * This function use the following smart strategy to detect the MIME-type:
   * <ul>
   * <li>It firstly try to detect the possible MIME-types from the filename
   * extension.</li>
   * <li>If there is one and only one candidate MIME-type detected from the
   * filename extension, and the argument {@code alwaysCheckMagic} is
   * {@code true}, the function returns that MIME-type.</li>
   * <li>Otherwise the function will try to detect the possible MIME-types from
   * the file content.</li>
   * <li>If there is no candidate MIME-type detected from the filename extension,
   * nor from the file content, the function returns {@code null}.</li>
   * <li>If there is no candidate MIME-type detected from the filename extension,
   * but there is one or more candidate MIME-types detected from the file content,
   * the function returns the first candidate MIME-type detected from the file
   * content.</li>
   * <li>If there is any common MIME-type detected <b>both</b> from the filename
   * extension, and from the file content, the function returns the first common
   * MIME-type.</li>
   * <li>Otherwise, the function returns the first candidate MIME-type detected
   * from the filename content.</li>
   * </ul>
   *
   * @param content
   *     the bytes of the content of the file to be detected.
   * @param filename
   *     the optional filename of the file to be detected; or {@code null} if
   *     the filename is unknown. If the filename is provided, the function will
   *     try to guess the MIME-type from the filename extension. Note that this
   *     argument must be the filename without any path component. Use
   *     {@link FilenameUtils#getFilename(File)} or
   *     {@link FilenameUtils#getFilename(Path)} to extract the filename from
   *     a {@link File} or a {@link Path}.
   * @param alwaysCheckMagic
   *     whether to always check the magic number of the file, even if the
   *     filename extension is recognized.
   * @return
   *     the canonical name of the detected MIME-type of the file, or
   *     {@code null} if the MIME-type cannot be detected.
   * @see #detect(File, String, boolean)
   * @see #detect(Path, String, boolean)
   * @see #detect(URL, String, boolean)
   * @see #detect(URI, String, boolean)
   * @see #detect(InputStream, String, boolean)
   * @see FilenameUtils#getFilename(File)
   * @see FilenameUtils#getFilename(Path)
   */
  @Nullable
  String detect(@Nonnull byte[] content, @Nullable String filename, boolean alwaysCheckMagic);
}
