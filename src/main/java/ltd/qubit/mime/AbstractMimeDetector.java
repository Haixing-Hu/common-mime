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
 * MIME类型检测器的抽象基类。
 *
 * @author 胡海星
 */
@SuppressWarnings("overloads")
public abstract class AbstractMimeDetector implements MimeDetector {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected boolean alwaysCheckMagicByDefault = false;

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAlwaysCheckMagicByDefault() {
    return alwaysCheckMagicByDefault;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAlwaysCheckMagicByDefault(final boolean alwaysCheckMagicByDefault) {
    this.alwaysCheckMagicByDefault = alwaysCheckMagicByDefault;
  }

  /**
   * 文件名获取函数接口。
   *
   * @param <T> 
   *     输入参数的类型。
   */
  interface GetFilenameFunctor<T> {
    /**
     * 应用此函数获取文件名。
     *
     * @param arg
     *     输入参数。
     * @return
     *     从输入参数中获取的文件名。
     */
    String apply(T arg);
  }

  /**
   * 实现根据文件名检测MIME类型的方法。
   *
   * @param <T>
   *     参数类型。
   * @param arg
   *     要检测的文件对象。
   * @param getFilenameFunctor
   *     从文件对象中获取文件名的函数。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   */
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

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public final String detectByFilename(@Nonnull final File file) {
    requireNonNull("file", file);
    return detectByFilenameImpl(file, FilenameUtils::getFilename);
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public final String detectByFilename(@Nonnull final Path path) {
    requireNonNull("path", path);
    return detectByFilenameImpl(path, FilenameUtils::getFilename);
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public final String detectByFilename(@Nonnull final String path) {
    requireNonNull("path", path);
    return detectByFilenameImpl(path, FilenameUtils::getFilenameFromPath);
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public final String detectByFilename(@Nonnull final URL url) {
    requireNonNull("url", url);
    return detectByFilenameImpl(url, FilenameUtils::getFilename);
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public final String detectByFilename(@Nonnull final URI uri) {
    requireNonNull("uri", uri);
    return detectByFilenameImpl(uri, FilenameUtils::getFilename);
  }

  /**
   * 从内容猜测MIME类型的函数接口。
   *
   * @param <T>
   *     输入参数的类型。
   */
  interface GuessFromContentFunctor<T> {
    /**
     * 应用此函数从内容猜测MIME类型。
     *
     * @param arg
     *     输入参数。
     * @return
     *     可能的MIME类型列表。
     * @throws IOException
     *     如果发生任何I/O错误。
     */
    List<String> apply(T arg) throws IOException;
  }

  /**
   * 实现根据文件内容检测MIME类型的方法。
   *
   * @param <T>
   *     参数类型。
   * @param arg
   *     要检测的文件对象。
   * @param guessFromContentFunctor
   *     从文件内容中猜测MIME类型的函数。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   */
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

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public final String detectByContent(@Nonnull final File file) throws IOException {
    requireNonNull("file", file);
    return detectByContentImpl(file, (File f) -> this.guessFromContent(f));
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public final String detectByContent(@Nonnull final Path path) throws IOException {
    requireNonNull("path", path);
    return detectByContentImpl(path, (Path p) -> this.guessFromContent(p));
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public final String detectByContent(@Nonnull final InputStream input)
      throws IOException {
    requireNonNull("input", input);
    return detectByContentImpl(input, (InputStream i) -> this.guessFromContent(i));
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * 打开流的函数接口。
   *
   * @param <T>
   *     输入参数的类型。
   */
  interface OpenStreamFunctor<T> {
    /**
     * 应用此函数打开输入流。
     *
     * @param arg
     *     输入参数。
     * @return
     *     打开的输入流。
     * @throws IOException
     *     如果发生任何I/O错误。
     */
    InputStream apply(T arg) throws IOException;
  }

  /**
   * 实现根据文件内容检测MIME类型的方法（需要打开流的版本）。
   *
   * @param <T>
   *     参数类型。
   * @param arg
   *     要检测的文件对象。
   * @param openStreamFunctor
   *     从文件对象中打开输入流的函数。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   */
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

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public final String detectByContent(@Nonnull final URL url) throws IOException {
    requireNonNull("url", url);
    return detectByContentImpl(url, (URL u) -> UrlUtils.openStream(u));
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public final String detectByContent(@Nonnull final URI uri) throws IOException {
    requireNonNull("url", uri);
    return detectByContentImpl(uri, (URI u) -> UrlUtils.openStream(u));
  }

  /**
   * 实现同时根据文件名和内容检测MIME类型的方法。
   *
   * @param <T>
   *     参数类型。
   * @param arg
   *     要检测的文件对象。
   * @param filename
   *     文件名，如果为null则从arg中获取。
   * @param alwaysCheckMagic
   *     是否总是检查魔术数字。
   * @param getFilenameFunctor
   *     从文件对象中获取文件名的函数。
   * @param guessFromContentFunctor
   *     从文件内容中猜测MIME类型的函数。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   */
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

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public final String detect(@Nonnull final File file,
      @Nullable final String filename, final boolean alwaysCheckMagic)
      throws IOException {
    requireNonNull("file", file);
    return detectImpl(file, filename, alwaysCheckMagic,
        FilenameUtils::getFilename, (File f) -> this.guessFromContent(f));
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public final String detect(@Nonnull final Path path,
      @Nullable final String filename, final boolean alwaysCheckMagic)
      throws IOException {
    requireNonNull("path", path);
    return detectImpl(path, filename, alwaysCheckMagic,
        FilenameUtils::getFilename, (Path p) -> this.guessFromContent(p));
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public final String detect(@Nonnull final InputStream input,
      @Nullable final String filename, final boolean alwaysCheckMagic)
      throws IOException {
    requireNonNull("input", input);
    return detectImpl(input, filename, alwaysCheckMagic,
        (i) -> filename, (InputStream i) -> this.guessFromContent(i));
  }

  /**
   * 实现同时根据文件名和内容检测MIME类型的方法（需要打开流的版本）。
   *
   * @param <T>
   *     参数类型。
   * @param arg
   *     要检测的文件对象。
   * @param filename
   *     文件名，如果为null则从arg中获取。
   * @param alwaysCheckMagic
   *     是否总是检查魔术数字。
   * @param getFilenameFunctor
   *     从文件对象中获取文件名的函数。
   * @param openStreamFunctor
   *     从文件对象中打开输入流的函数。
   * @return
   *     检测到的文件MIME类型的规范名称，如果无法检测到MIME类型，则返回{@code null}。
   * @throws IOException
   *     如果发生任何I/O错误。
   */
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

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public final String detect(@Nonnull final URL url,
      @Nullable final String filename,
      final boolean alwaysCheckMagic) throws IOException {
    requireNonNull("url", url);
    return detectImpl(url, filename, alwaysCheckMagic,
        FilenameUtils::getFilename, (URL u) -> UrlUtils.openStream(u));
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public final String detect(@Nonnull final URI uri,
      @Nullable final String filename,
      final boolean alwaysCheckMagic) throws IOException {
    requireNonNull("url", uri);
    return detectImpl(uri, filename, alwaysCheckMagic,
        FilenameUtils::getFilename, (URI u) -> UrlUtils.openStream(u));
  }

  /**
   * {@inheritDoc}
   */
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
   * 合并从文件名扩展名和文件内容检测到的结果。
   * <p>
   * 此函数应由此类的实现使用，以合并从文件名扩展名和文件内容检测到的结果。
   *
   * @param fromExtension
   *     从文件名扩展名检测到的结果。
   * @param fromContent
   *     从文件内容检测到的结果。
   * @return
   *     合并后的结果，如果无法检测到MIME类型，则返回{@code null}。
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
   * 从文件名扩展名猜测文件可能的MIME类型。
   *
   * @param filename
   *     要检测的文件的名称，不应包含任何路径分隔符，且不应为{@code null}或空。
   * @return
   *     文件可能的MIME类型的规范名称列表，如果无法检测到MIME类型，则返回空列表。
   */
  @Nonnull
  protected abstract List<String> guessFromFilename(@Nonnull String filename);

  /**
   * 从文件内容猜测文件可能的MIME类型。
   *
   * @param file
   *     要检测的文件。
   * @return
   *     文件可能的MIME类型的规范名称列表，如果无法检测到MIME类型，则返回空列表。
   * @throws IOException
   *     如果发生任何I/O错误。
   */
  @Nonnull
  protected abstract List<String> guessFromContent(@Nonnull File file)
      throws IOException;

  /**
   * 从文件内容猜测文件可能的MIME类型。
   *
   * @param path
   *     要检测的文件路径。
   * @return
   *     文件可能的MIME类型的规范名称列表，如果无法检测到MIME类型，则返回空列表。
   * @throws IOException
   *     如果发生任何I/O错误。
   */
  @Nonnull
  protected abstract List<String> guessFromContent(@Nonnull Path path)
      throws IOException;

  /**
   * 从文件内容猜测文件可能的MIME类型。
   *
   * @param input
   *     要检测的文件内容的输入流。
   * @return
   *     文件可能的MIME类型的规范名称列表，如果无法检测到MIME类型，则返回空列表。
   * @throws IOException
   *     如果发生任何I/O错误。
   */
  @Nonnull
  protected abstract List<String> guessFromContent(@Nonnull InputStream input)
      throws IOException;
}