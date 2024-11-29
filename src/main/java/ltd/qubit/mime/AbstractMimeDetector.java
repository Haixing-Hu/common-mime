////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ltd.qubit.commons.error.UnexpectedError;
import ltd.qubit.commons.io.FilenameUtils;
import ltd.qubit.commons.lang.StringUtils;
import ltd.qubit.commons.net.UrlUtils;

import static ltd.qubit.commons.lang.Argument.requireNonNull;

/**
 * The abstract base class of MIME-type detectors.
 *
 * @author Haixing Hu
 */
@SuppressWarnings("overloads")
public abstract class AbstractMimeDetector implements MimeDetector {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected boolean alwaysCheckMagicByDefault = false;

  @Override
  public boolean isAlwaysCheckMagicByDefault() {
    return alwaysCheckMagicByDefault;
  }

  @Override
  public void setAlwaysCheckMagicByDefault(final boolean alwaysCheckMagicByDefault) {
    this.alwaysCheckMagicByDefault = alwaysCheckMagicByDefault;
  }

  interface GetFilenameFunctor<T> {
    String apply(T arg);
  }

  private <T> String detectByFilenameImpl(final T arg,
      final GetFilenameFunctor<T> getFilenameFunctor) {
    logger.debug("Detecting the MIME-type of the file from its filename extension: {}", arg);
    final String filename = getFilenameFunctor.apply(arg);
    if (StringUtils.isEmpty(filename)) {
      logger.error("No MIME-type detected from the filename extension since the "
          + "filename of the file is empty: {}", arg);
      return null;
    }
    final List<String> candidates = guessFromFilename(filename);
    if (candidates.isEmpty()) {
      logger.error("No MIME-type detected from the filename extension of the file: {}", arg);
      return null;
    } else {
      logger.debug("All possible MIME-types detected from the filename extension are: {}", candidates);
      final String result = candidates.get(0);
      logger.debug("Use the first MIME-type detected from the filename extension: {}", result);
      return result;
    }
  }

  @Nullable
  @Override
  public final String detectByFilename(@Nonnull final File file) {
    requireNonNull("file", file);
    return detectByFilenameImpl(file, FilenameUtils::getFilename);
  }

  @Nullable
  @Override
  public final String detectByFilename(@Nonnull final Path path) {
    requireNonNull("path", path);
    return detectByFilenameImpl(path, FilenameUtils::getFilename);
  }

  @Nullable
  @Override
  public final String detectByFilename(@Nonnull final String path) {
    requireNonNull("path", path);
    return detectByFilenameImpl(path, FilenameUtils::getFilenameFromPath);
  }

  @Nullable
  @Override
  public final String detectByFilename(@Nonnull final URL url) {
    requireNonNull("url", url);
    return detectByFilenameImpl(url, FilenameUtils::getFilename);
  }

  @Nullable
  @Override
  public final String detectByFilename(@Nonnull final URI uri) {
    requireNonNull("uri", uri);
    return detectByFilenameImpl(uri, FilenameUtils::getFilename);
  }

  interface GuessFromContentFunctor<T> {
    List<String> apply(T arg) throws IOException;
  }

  private <T> String detectByContentImpl(final T arg,
      final GuessFromContentFunctor<T> guessFromContentFunctor) throws IOException {
    logger.debug("Detecting the MIME-type of the file from its content: {}", arg);
    final List<String> candidates = guessFromContentFunctor.apply(arg);
    if (candidates.isEmpty()) {
      logger.error("No MIME-type detected from the content of the file: {}", arg);
      return null;
    } else {
      logger.debug("All possible MIME-types detected from the content are: {}", candidates);
      final String result = candidates.get(0);
      logger.debug("Use the first MIME-type detected from the content: {}", result);
      return result;
    }
  }

  @Nullable
  @Override
  public final String detectByContent(@Nonnull final File file) throws IOException {
    requireNonNull("file", file);
    return detectByContentImpl(file, (File f) -> this.guessFromContent(f));
  }

  @Nullable
  @Override
  public final String detectByContent(@Nonnull final Path path) throws IOException {
    requireNonNull("path", path);
    return detectByContentImpl(path, (Path p) -> this.guessFromContent(p));
  }

  @Nullable
  @Override
  public final String detectByContent(@Nonnull final InputStream input)
      throws IOException {
    requireNonNull("input", input);
    return detectByContentImpl(input, (InputStream i) -> this.guessFromContent(i));
  }

  @Nullable
  @Override
  public final String detectByContent(@Nonnull final byte[] content) {
    requireNonNull("content", content);
    final ByteArrayInputStream in = new ByteArrayInputStream(content);
    final List<String> candidates;
    try {
      candidates = guessFromContent(in);
    } catch (final IOException e) {
      throw new UnexpectedError("Should NEVER throw IOException: " + e.getMessage(), e);
    }
    return candidates.isEmpty() ? null : candidates.get(0);
  }

  interface OpenStreamFunctor<T> {
    InputStream apply(T arg) throws IOException;
  }

  private <T> String detectByContentImpl(final T arg,
      final OpenStreamFunctor<T> openStreamFunctor) throws IOException {
    logger.debug("Detecting the MIME-type of the file from its content: {}", arg);
    final List<String> candidates;
    try (final InputStream in = openStreamFunctor.apply(arg)) {
      candidates = guessFromContent(in);
    }
    if (candidates.isEmpty()) {
      logger.error("No MIME-type detected from the content of the file: {}", arg);
      return null;
    } else {
      logger.debug("All possible MIME-types detected from the content are: {}", candidates);
      final String result = candidates.get(0);
      logger.debug("Use the first MIME-type detected from the content: {}", result);
      return result;
    }
  }

  @Nullable
  @Override
  public final String detectByContent(@Nonnull final URL url) throws IOException {
    requireNonNull("url", url);
    return detectByContentImpl(url, (URL u) -> UrlUtils.openStream(u));
  }

  @Nullable
  @Override
  public final String detectByContent(@Nonnull final URI uri) throws IOException {
    requireNonNull("url", uri);
    return detectByContentImpl(uri, (URI u) -> UrlUtils.openStream(u));
  }

  private <T> String detectImpl(final T arg, @Nullable final String filename,
      final boolean alwaysCheckMagic,
      final GetFilenameFunctor<T> getFilenameFunctor,
      final GuessFromContentFunctor<T> guessFromContentFunctor) throws IOException {
    logger.debug("Detecting the MIME-type of the file from its filename extension and its content: {}", arg);
    final String theFilename = (filename != null ? filename : getFilenameFunctor.apply(arg));
    final List<String> fromExtension;
    if (StringUtils.isEmpty(theFilename)) {
      fromExtension = Collections.emptyList();
    } else {
      fromExtension = guessFromFilename(theFilename);
    }
    // if there is only one candidate, and we don't need to always check the
    // file magic, then return the only candidate directly.
    if ((fromExtension.size() == 1) && (!alwaysCheckMagic)) {
      logger.debug("All possible MIME-types detected from the file extension are: {}", fromExtension);
      final String result = fromExtension.get(0);
      logger.debug("Use the MIME-type detected from the filename extension: {}", result);
      return result;
    }
    // try to guess the MIME-type from the file content
    final List<String> fromContent = guessFromContentFunctor.apply(arg);
    // now we try to combine the results from the filename extension and the
    // file content.
    return mergeResults(fromExtension, fromContent);
  }

  @Nullable
  @Override
  public final String detect(@Nonnull final File file,
      @Nullable final String filename, final boolean alwaysCheckMagic)
      throws IOException {
    requireNonNull("file", file);
    return detectImpl(file, filename, alwaysCheckMagic,
        FilenameUtils::getFilename, (File f) -> this.guessFromContent(f));
  }

  @Nullable
  @Override
  public final String detect(@Nonnull final Path path,
      @Nullable final String filename, final boolean alwaysCheckMagic)
      throws IOException {
    requireNonNull("path", path);
    return detectImpl(path, filename, alwaysCheckMagic,
        FilenameUtils::getFilename, (Path p) -> this.guessFromContent(p));
  }

  @Nullable
  @Override
  public final String detect(@Nonnull final InputStream input,
      @Nullable final String filename, final boolean alwaysCheckMagic)
      throws IOException {
    requireNonNull("input", input);
    return detectImpl(input, filename, alwaysCheckMagic,
        (i) -> filename, (InputStream i) -> this.guessFromContent(i));
  }

  private <T> String detectImpl(final T arg,
      @Nullable final String filename,
      final boolean alwaysCheckMagic,
      final GetFilenameFunctor<T> getFilenameFunctor,
      final OpenStreamFunctor<T> openStreamFunctor) throws IOException {
    logger.debug("Detecting the MIME-type of the file from its filename extension and its content: {}", arg);
    final String theFilename = (filename != null ? filename : getFilenameFunctor.apply(arg));
    final List<String> fromExtension;
    if (StringUtils.isEmpty(theFilename)) {
      fromExtension = Collections.emptyList();
    } else {
      fromExtension = guessFromFilename(theFilename);
    }
    // if there is only one candidate, and we don't need to always check the
    // file magic, then return the only candidate directly.
    if ((fromExtension.size() == 1) && (!alwaysCheckMagic)) {
      logger.debug("All possible MIME-types detected from the file extension are: {}", fromExtension);
      final String result = fromExtension.get(0);
      logger.debug("Use the MIME-type detected from the filename extension: {}", result);
      return result;
    }
    // try to guess the MIME-type from the file content
    final List<String> fromContent;
    try (final InputStream in = openStreamFunctor.apply(arg)) {
      fromContent = guessFromContent(in);
    }
    // now we try to combine the results from the filename extension and the
    // file content.
    return mergeResults(fromExtension, fromContent);
  }

  @Nullable
  @Override
  public final String detect(@Nonnull final URL url,
      @Nullable final String filename,
      final boolean alwaysCheckMagic) throws IOException {
    requireNonNull("url", url);
    return detectImpl(url, filename, alwaysCheckMagic,
        FilenameUtils::getFilename, (URL u) -> UrlUtils.openStream(u));
  }

  @Nullable
  @Override
  public final String detect(@Nonnull final URI uri,
      @Nullable final String filename,
      final boolean alwaysCheckMagic) throws IOException {
    requireNonNull("url", uri);
    return detectImpl(uri, filename, alwaysCheckMagic,
        FilenameUtils::getFilename, (URI u) -> UrlUtils.openStream(u));
  }

  @Nullable
  @Override
  public final String detect(@Nonnull final byte[] content, @Nullable final String filename,
      final boolean alwaysCheckMagic) {
    requireNonNull("content", content);
    try {
      return detect(new ByteArrayInputStream(content), filename, alwaysCheckMagic);
    } catch (final IOException e) {
      throw new UnexpectedError("Should NEVER throw IOException: " + e.getMessage(), e);
    }
  }

  /**
   * Merges the results detected from the filename extension and from the file
   * content.
   * <p>
   * This function should be used by the implementations of this class to merge
   * the results detected from the filename extension and from the file content.
   *
   * @param fromExtension
   *     the results detected from the filename extension.
   * @param fromContent
   *     the results detected from the file content.
   * @return
   *     the merged results, or {@code null} if no MIME-type can be detected.
   */
  protected final String mergeResults(final List<String> fromExtension,
      final List<String> fromContent) {
    // now we try to combine the results from the filename extension and the
    // file content.
    if (fromExtension.isEmpty()) {
      if (fromContent.isEmpty()) {
        logger.error("No MIME-type detected from the filename extension nor from the file content.");
        return null;
      } else {
        logger.debug("No MIME-type detected from the filename extension.");
        logger.debug("All possible MIME-types detected from the file content are: {}", fromContent);
        final String result = fromContent.get(0);
        logger.debug("Use the first MIME-type detected from the file content: {}", result);
        return result;
      }
    } else if (fromContent.isEmpty()) {
      logger.debug("No MIME-type detected from the file content.");
      logger.debug("All possible MIME-types detected from the file extension are: {}", fromExtension);
      final String result = fromExtension.get(0);
      logger.debug("Use the first MIME-type detected from the filename extension: {}", result);
      return result;
    } else {
      logger.debug("All possible MIME-types detected from the file extension are: {}", fromExtension);
      logger.debug("All possible MIME-types detected from the file content are: {}", fromContent);
      // if there are both candidates from the filename extension and the file
      // content, then return the common candidates.
      String result = fromExtension
          .stream()
          .filter(fromContent::contains)
          .findFirst()
          .orElse(null);
      if (result != null) {
        logger.debug("Use the first MIME-type detected from both the filename "
            + "extension and the file content: {}", result);
        return result;
      } else {
        result = fromContent.get(0);
        logger.debug("No MIME-type detected from both the filename extension and the file content.");
        logger.debug("Use the first MIME-type detected from the file content: {}", result);
        return result;
      }
    }
  }

  /**
   * Guesses the possible MIME-types of a file from its filename extension.
   *
   * @param filename
   *     the name of the file to be detected, which should not contain any path
   *     separator, and should not be {@code null} nor empty.
   * @return
   *     the list of canonical names of possible MIME-types of the file, or
   *     an empty list if the MIME-type cannot be detected.
   */
  @Nonnull
  protected abstract List<String> guessFromFilename(@Nonnull String filename);

  /**
   * Guesses the possible MIME-types of a file from its file content.
   *
   * @param file
   *     the file to be detected.
   * @return
   *     the list of canonical names of possible MIME-types of the file, or
   *     an empty list if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   */
  @Nonnull
  protected abstract List<String> guessFromContent(@Nonnull File file)
      throws IOException;

  /**
   * Guesses the possible MIME-types of a file from its file content.
   *
   * @param path
   *     the path of the file to be detected.
   * @return
   *     the list of canonical names of possible MIME-types of the file, or
   *     an empty list if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   */
  @Nonnull
  protected abstract List<String> guessFromContent(@Nonnull Path path)
      throws IOException;

  /**
   * Guesses the possible MIME-types of a file from its file content.
   *
   * @param input
   *     the input stream of the file content to be detected.
   * @return
   *     the list of canonical names of possible MIME-types of the file, or
   *     an empty list if the MIME-type cannot be detected.
   * @throws IOException
   *     if any I/O error occurs.
   */
  @Nonnull
  protected abstract List<String> guessFromContent(@Nonnull InputStream input)
      throws IOException;
}
