////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2017 - 2024.
//    Nanjing Smart Medical Investment Operation Service Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import ltd.qubit.commons.config.Config;
import ltd.qubit.commons.config.error.XmlConfigurationError;
import ltd.qubit.commons.io.FileUtils;
import ltd.qubit.commons.io.FilenameUtils;
import ltd.qubit.commons.io.IoUtils;
import ltd.qubit.commons.text.tostring.ToStringBuilder;
import ltd.qubit.commons.text.xml.DomUtils;
import ltd.qubit.commons.text.xml.XmlException;
import ltd.qubit.commons.text.xml.XmlUtils;
import ltd.qubit.commons.util.Version;
import ltd.qubit.commons.util.VersionSignature;
import ltd.qubit.commons.util.pair.Pair;

import static ltd.qubit.commons.io.InputUtils.readList;
import static ltd.qubit.commons.io.OutputUtils.writeCollection;
import static ltd.qubit.commons.lang.Argument.requireNonNull;
import static ltd.qubit.commons.lang.Argument.requirePositive;

/**
 * The MIME-type repository is a singleton registry of all MIME-types.
 * <p>
 * TODO: this implementation can NOT handle the MIME-type of a directory, which
 *   has a directory tree glob pattern.
 *
 * @see <a href='http://standards.freedesktop.org/shared-mime-info-spec/shared-mime-info-spec-0.19.html'>Shared MIME-info Database</a>
 * @see <a href='http://www.freedesktop.org/wiki/Software/shared-mime-info'>shared-mime-info</a>
 * @author Haixing Hu
 */
@NotThreadSafe
public class MimeRepository implements Serializable {

  private static final long serialVersionUID = - 6880339145050773602L;

  /**
   * The value of this property decides whether the MIME registry will be
   * rebuilt and ignore the existing serialized registry.
   * <p>
   * <table border="1">
   * <caption></caption>
   * <tr>
   * <th>Type</th>
   * <th>Count</th>
   * <th>Value</th>
   * <th>Required</th>
   * <th>Default</th>
   * <th>Range</th>
   * </tr>
   * <tr>
   * <td>boolean</td>
   * <td>1</td>
   * <td>whether the MIME registry will be rebuild and ignore the existing
   * serialized registry.</td>
   * <td>no</td>
   * <td>{@link #DEFAULT_REBUILD}</td>
   * <td></td>
   * </tr>
   * </table>
   *
   * @see #DEFAULT_REBUILD
   */
  public static final String PROPERTY_REBUILD = "ltd.qubit.mime.repository.MimeRepository.rebuild";

  /**
   * The default value of the property {@link #PROPERTY_REBUILD}.
   *
   * @see #PROPERTY_REBUILD
   */
  public static final boolean DEFAULT_REBUILD = false;

  /**
   * The value of this property decides whether to save the MIME-type repository
   * after rebuilding the repository.
   * <p>
   * <table border="1">
   * <caption></caption>
   * <tr>
   * <th>Type</th>
   * <th>Count</th>
   * <th>Value</th>
   * <th>Required</th>
   * <th>Default</th>
   * <th>Range</th>
   * </tr>
   * <tr>
   * <td>boolean</td>
   * <td>1</td>
   * <td>whether to save the MIME-type repository after rebuilding the
   * repository.</td>
   * <td>no</td>
   * <td>{@link #DEFAULT_SAVE}</td>
   * <td></td>
   * </tr>
   * </table>
   *
   * @see #DEFAULT_SAVE
   */
  public static final String PROPERTY_SAVE = "ltd.qubit.mime.repository.MimeRepository.save";

  /**
   * The default value of the property {@link #PROPERTY_SAVE}.
   *
   * @see #PROPERTY_SAVE
   */
  public static final boolean DEFAULT_SAVE = false;

  /**
   * If the value of this property decides whether the detection of MIME-type by
   * filename and content will check the magic by default.
   * <p>
   * <table border="1">
   * <caption></caption>
   * <tr>
   * <th>Type</th>
   * <th>Count</th>
   * <th>Value</th>
   * <th>Required</th>
   * <th>Default</th>
   * <th>Range</th>
   * </tr>
   * <tr>
   * <td>boolean</td>
   * <td>1</td>
   * <td>whether the detection of MIME-type by filename and content will check
   * the magic by default.</td>
   * <td>no</td>
   * <td>{@link #DEFAULT_CHECK_MAGIC}</td>
   * <td></td>
   * </tr>
   * </table>
   *
   * @see #DEFAULT_CHECK_MAGIC
   */
  public static final String PROPERTY_CHECK_MAGIC = "ltd.qubit.mime.repository.MimeRepository.checkMagic";

  /**
   * The default value of the property {@link #PROPERTY_CHECK_MAGIC}.
   *
   * @see #PROPERTY_CHECK_MAGIC
   */
  public static final boolean DEFAULT_CHECK_MAGIC = false;

  /**
   * The value of this property specifies the absolute pathname of the file
   * where to store the serialized MIME-type registry.
   * <p>
   * <table border="1">
   * <caption></caption>
   * <tr>
   * <th>Type</th>
   * <th>Count</th>
   * <th>Value</th>
   * <th>Required</th>
   * <th>Default</th>
   * <th>Range</th>
   * </tr>
   * <tr>
   * <td>string</td>
   * <td>1</td>
   * <td>the absolute pathname of the file where to store the serialized
   * MIME-type registry.</td>
   * <td>no</td>
   * <td>{@link #DEFAULT_SERIALIZATION}</td>
   * <td></td>
   * </tr>
   * </table>
   *
   * @see #DEFAULT_SERIALIZATION
   */
  public static final String PROPERTY_SERIALIZATION = "ltd.qubit.mime.repository.MimeRepository.serialization";

  /**
   * The default value of the property {@link #PROPERTY_SERIALIZATION}.
   *
   * @see #PROPERTY_SERIALIZATION
   */
  public static final String DEFAULT_SERIALIZATION = "${user.home}/.njzhyl/MimeRepository.ser";

  /**
   * The value of this property specifies the resource name of the XML format
   * MIME-type database file.
   * <p>
   * <table border="1">
   * <caption></caption>
   * <tr>
   * <th>Type</th>
   * <th>Count</th>
   * <th>Value</th>
   * <th>Required</th>
   * <th>Default</th>
   * <th>Range</th>
   * </tr>
   * <tr>
   * <td>string</td>
   * <td>1</td>
   * <td>the resource name of the XML format MIME-type database file.</td>
   * <td>no</td>
   * <td>${user.home}/.njzhyl/MimeTypeRepository.ser</td>
   * <td></td>
   * </tr>
   * </table>
   */
  public static final String PROPERTY_DATABASE = "ltd.qubit.mime.repository.MimeRepository.database";

  /**
   * The value of this property specifies the default binary MIME-type name.
   * <p>
   * <table border="1">
   * <caption></caption>
   * <tr>
   * <th>Type</th>
   * <th>Count</th>
   * <th>Value</th>
   * <th>Required</th>
   * <th>Default</th>
   * <th>Range</th>
   * </tr>
   * <tr>
   * <td>string</td>
   * <td>1</td>
   * <td>the default binary MIME-type name.</td>
   * <td>no</td>
   * <td>application/octet-stream</td>
   * <td></td>
   * </tr>
   * </table>
   */
  public static final String PROPERTY_DEFAULT_BINARY = "ltd.qubit.mime.repository.MimeRepository.defaultBinary";

  /**
   * The value of this property specifies the default text MIME-type name.
   * <p>
   * <table border="1">
   * <caption></caption>
   * <tr>
   * <th>Type</th>
   * <th>Count</th>
   * <th>Value</th>
   * <th>Required</th>
   * <th>Default</th>
   * <th>Range</th>
   * </tr>
   * <tr>
   * <td>string</td>
   * <td>1</td>
   * <td>default text MIME-type name.</td>
   * <td>no</td>
   * <td>text/plain</td>
   * <td></td>
   * </tr>
   * </table>
   */
  public static final String PROPERTY_DEFAULT_TEXT = "ltd.qubit.mime.repository.MimeRepository.defaultText";

  public static final String ROOT_NODE = "mime-info";

  public static final Version VERSION = new Version(0, 1, 0, 0);

  private static final VersionSignature SIGNATURE = new VersionSignature(serialVersionUID, VERSION);

  private static volatile MimeRepository instance = null;

  public static MimeRepository getInstance() {
    // use the double-checked locking trick
    if (instance == null) {
      synchronized (MimeRepository.class) {
        if (instance == null) {
          final Config config = MimeConfig.get();
          instance = new MimeRepository(config);
        }
      }
    }
    return instance;
  }

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private Config config;
  private List<MimeType> mimeList;
  private Map<String, MimeType> mimeNameMap;
  private Multimap<String, Pair<MimeGlob, MimeType>> literalGlobMap;
  private Multimap<String, Pair<MimeGlob, MimeType>> extensionGlobMap;
  private List<Pair<MimeGlob, MimeType>> otherGlobList;
  private boolean alwaysCheckMagic;
  private int maxTestBytes;

  private MimeRepository() {
    mimeList = new LinkedList<>();
    mimeNameMap = new HashMap<>();
    literalGlobMap = LinkedHashMultimap.create();
    extensionGlobMap = LinkedHashMultimap.create();
    otherGlobList = new LinkedList<>();
    maxTestBytes = 0;
    alwaysCheckMagic = false;
  }

  private MimeRepository(final Config config) {
    this();
    this.config = config;
    alwaysCheckMagic = config.getBoolean(PROPERTY_CHECK_MAGIC, DEFAULT_CHECK_MAGIC);
    // load or rebuild the repository
    if (config.getBoolean(PROPERTY_REBUILD, DEFAULT_REBUILD)) {
      // need to rebuild the repository
      rebuild(config);
    } else { // try to load the serialized repository
      final String serialization = config.getString(PROPERTY_SERIALIZATION, DEFAULT_SERIALIZATION);
      final File file = new File(serialization);
      try {
        load(file);
      } catch (final IOException e) {
        logger.warn("Failed to load the saved MIME-type repository, rebuild it： {}",
            e.toString());
        rebuild(config);
      }
    }
  }

  public void rebuild() {
    rebuild(config);
  }

  protected void load(final File file) throws IOException {
    logger.info("Loading the serialized MIME-type repository from {} ...", file);
    final long start = System.currentTimeMillis();
    InputStream in = null;
    try {
      in = new FileInputStream(file);
      in = new BufferedInputStream(in);
      SIGNATURE.verify(in);
      mimeList = readList(MimeType.class, in, false, false, mimeList);
      if (mimeList != null) {
        for (final MimeType mime : mimeList) {
          this.addMimeType(mime);
        }
      }
    } finally {
      IoUtils.closeQuietly(in);
    }
    final long end = System.currentTimeMillis();
    logger.info("Successfully load the serialized MIME-type repository in {} milliseconds.", end - start);
    logStatistics();
  }

  private void logStatistics() {
    logger.info("Totally {} MIME-type entries.", mimeList.size());
    logger.info("Totally {} literal globs.", literalGlobMap.size());
    logger.info("Totally {} extension globs.", extensionGlobMap.size());
    logger.info("Totally {} other globs.", otherGlobList.size());
  }

  protected void store(final File file) throws IOException {
    logger.info("Storing the serialized MIME-type repository to {} ...", file);
    final long start = System.currentTimeMillis();
    OutputStream out = null;
    try {
      FileUtils.ensureParentExist(file);
      out = new FileOutputStream(file);
      out = new BufferedOutputStream(out);
      SIGNATURE.sign(out);
      writeCollection(MimeType.class, out, mimeList);
    } finally {
      IoUtils.closeQuietly(out);
    }
    final long end = System.currentTimeMillis();
    logger.info("Successfully store the serialized MIME-type " +
    		"repository in {} milliseconds.", end - start);
  }

  protected void rebuild(final Config config) {
    final String database = config.getString(PROPERTY_DATABASE);
    logger.info("Rebuilding the MIME-type repository ...");
    final long start = System.currentTimeMillis();
    try {
      final Document doc = XmlUtils.parse(database, MimeRepository.class);
      final Element root = doc.getDocumentElement();
      fromXml(root);
    } catch (final XmlException e) {
      logger.error( "Failed to parse the MIME-type database: {}", e.getMessage(), e);
      throw new XmlConfigurationError(database, e);
    }
    final long end = System.currentTimeMillis();
    logger.info("Successfully rebuild MIME-type repository in {} milliseconds.", end - start);
    logStatistics();
    if (config.getBoolean(PROPERTY_SAVE, DEFAULT_SAVE)) {
      final String serialization = config.getString(PROPERTY_SERIALIZATION);
      try {
        store(new File(serialization));
      } catch (final IOException e) {
        logger.warn( "Failed to store the MIME-type repository: {}", e.getMessage());
        // ignore the IOException
      }
    }
  }

  public MimeType get(final String name) {
    return mimeNameMap.get(name.toLowerCase());
  }

  public List<MimeType> getAll() {
    return Collections.unmodifiableList(mimeList);
  }

  /**
   * Returns the maximum number of bytes need to be tested in order to determinate
   * the correct MIME type using the MIME magic.
   *
   * @return The maximum number of bytes need to be tested in order to determinate
   *         the correct MIME type using the MIME magic.
   */
  public int getMaxTestBytes() {
    return maxTestBytes;
  }

  /**
   * Detect the MIME-type from the extension of a filename.
   * <p>
   * The following rules should be obeyed by the detection procedure:
   * <p>
   * Applications MUST match globs case-insensitively, except when the
   * case-sensitive attribute is set to true. This is so that e.g. main.C will
   * be seen as a C++ file, but IMAGE.GIF will still use the *.gif pattern.
   * <p>
   * If several patterns of the same weight match then the longest pattern
   * SHOULD be used. In particular, files with multiple extensions (such as
   * Data.tar.gz) MUST match the longest sequence of extensions (eg '*.tar.gz'
   * in preference to '*.gz'). Literal patterns (eg, 'Makefile') must be matched
   * before all others. It is suggested that patterns beginning with `*.' and
   * containing no other special characters (`*?[') should be placed in a hash
   * table for efficient lookup, since this covers the majority of the patterns.
   * Thus, patterns of this form should be matched before other wild-carded
   * patterns.
   * <p>
   * If a matching pattern is provided by two or more MIME types, applications
   * SHOULD not rely on one of them. They are instead supposed to use magic data
   * (see below) to detect the actual MIME type. This is for instance required
   * to deal with container formats like Ogg or AVI, that map various video
   * and/or audio-encoded data to one extension.
   * <p>
   * There may be several rules mapping to the same type. They should all be
   * merged. If the same pattern is defined twice, then they MUST be ordered by
   * the directory the rule came from, as described above.
   *
   * @param filename
   *     the filename of the file to be detected.
   * @return the list of all possible MIME-types for the specified filename; or
   *     {@code null} if no MIME-type matches the specified filename.
   */
  @Nullable
  public List<MimeType> detectByFilename(final String filename) {
    requireNonNull("filename", filename);
    // get the exact filename, convert it to lowercase
    final String exactFilename = FilenameUtils
        .getFilenameFromPath(filename)
        .toLowerCase();
    final GlobDetectionResult result = new GlobDetectionResult();
    // First, find in the exact glob patterns.
    if (literalGlobMap.containsKey(exactFilename)) {
      final Collection<Pair<MimeGlob, MimeType>> pairs = literalGlobMap.get(exactFilename);
      for (final Pair<MimeGlob, MimeType> pair : pairs) {
        result.compareAdd(pair.first, pair.second);
      }
    }
    // Second, find in the extension glob patterns.
    // Note that some filename may have more than one possible extension,
    // for example, "file.tar.gz", has both the extension "tar.gz" and "gz".
    int pos = exactFilename.indexOf('.');
    while (pos >= 0) {
      final String ext = exactFilename.substring(pos + 1);
      if (extensionGlobMap.containsKey(ext)) {
        final Collection<Pair<MimeGlob, MimeType>> pairs = extensionGlobMap.get(ext);
        for (final Pair<MimeGlob, MimeType> pair : pairs) {
          result.compareAdd(pair.first, pair.second);
        }
      }
      // get the next extension
      pos = exactFilename.indexOf('.', pos + 1);
    }
    // Third, find in the other glob patterns
    for (final Pair<MimeGlob, MimeType> pair : otherGlobList) {
      final MimeGlob glob = pair.first;
      if (glob.matches(exactFilename)) {
        result.compareAdd(glob, pair.second);
      }
    }
    if (result.list.isEmpty()) {
      return null;
    } else {
      return result.list;
    }
  }

  /**
   * Detect the MIME-type from the content of a file.
   *
   * @param buffer
   *     the buffer containing the content of the file.
   * @param n
   *     the number of bytes in the buffer.
   * @return
   *     the list of all possible MIME-types for the specified content; or
   *     {@code null} if no MIME-type matches the specified content.
   */
  @Nullable
  public List<MimeType> detectByContent(final byte[] buffer, final int n) {
    requireNonNull("buffer", buffer);
    requirePositive("n", n);
    return Utils.detectByMagic(buffer, n, mimeList);
  }

  /**
   * Detect the MIME-type from the content of a file.
   * <p>
   * <b>NOTE:</b> The input stream must support marking, and will not be closed
   * by this method.
   *
   * @param markSupportedInput
   *     the input stream of the content of the file. Note that this input
   *     stream must support marking, and this method will mark the input
   *     stream before reading the content, and reset the input stream after
   *     reading the content. It will not close the input stream.
   * @return
   *     the list of all possible MIME-types for the specified content; or
   *     {@code null} if no MIME-type matches the specified content.
   */
  @Nullable
  public List<MimeType> detectByContent(final InputStream markSupportedInput)
      throws IOException {
    requireNonNull("input", markSupportedInput);
    if (!markSupportedInput.markSupported()) {
      throw new IllegalArgumentException("The input stream must support marking.");
    }
    final byte[] buffer = new byte[maxTestBytes];
    markSupportedInput.mark(maxTestBytes);
    final int nBytes = markSupportedInput.read(buffer, 0, maxTestBytes);
    markSupportedInput.reset();
    return Utils.detectByMagic(buffer, nBytes, mimeList);
  }

  @Nullable
  public List<MimeType> detect(final String filename, final byte[] buffer, final int n) {
    return detect(filename, buffer, n, alwaysCheckMagic);
  }

  @Nullable
  public List<MimeType> detect(final String filename, final byte[] buffer,
      final int nBytes, final boolean alwaysCheckMagic) {
    final List<MimeType> list = detectByFilename(filename);
    if (list == null) {
      return Utils.detectByMagic(buffer, nBytes, mimeList);
    } else if ((!alwaysCheckMagic) && (list.size() == 1)) {
      return list;
    }
    // check each MIME-type in the mimeList returned by detection on filename,
    // test whether its or its super types' magic match the buffer.
    return Utils.checkByMagic(buffer, nBytes, list);
  }

  @Nullable
  public List<MimeType> detect(final String filename, final InputStream input)
      throws IOException {
    return detect(filename, input, alwaysCheckMagic);
  }

  @Nullable
  public List<MimeType> detect(final String filename, final InputStream input,
      final boolean alwaysCheckMagic) throws IOException {
    if (!input.markSupported()) {
      throw new IllegalArgumentException("The input stream must support marking.");
    }
    final List<MimeType> list = detectByFilename(filename);
    if (list == null) {
      final byte[] buffer = new byte[maxTestBytes];
      input.mark(maxTestBytes);
      final int nBytes = input.read(buffer, 0, maxTestBytes);
      input.reset();
      return Utils.detectByMagic(buffer, nBytes, mimeList);
    } else if ((!alwaysCheckMagic) && (list.size() == 1)) {
      return list;
    }
    // check each MIME-type in the mimeList returned by detection on filename,
    // test whether its or its super types' magic match the buffer.
    //
    final byte[] buffer = new byte[maxTestBytes];
    input.mark(maxTestBytes);
    final int nBytes = input.read(buffer, 0, maxTestBytes);
    input.reset();
    final List<MimeType> result = Utils.checkByMagic(buffer, nBytes, list);
    // note that if the mimeList has only one candidate, but that candidate
    // has no magic rule, the checkByMagic() may also return null. So we
    // need to deal with this case.
    if (result == null) {
      if (list.size() == 1) {
        return list;
      } else {
        return null;
      }
    } else {
      return result;
    }
  }

  @Nullable
  public List<MimeType> detect(final File file) throws IOException {
    try (final InputStream in = new BufferedInputStream(new FileInputStream(file))) {
      return detect(file.getName(), in);
    }
  }

  @Nullable
  public String detectFirstName(final File file) throws IOException {
    final List<MimeType> mimes = detect(file);
    if (mimes == null || mimes.isEmpty()) {
      return null;
    } else {
      return mimes.get(0).getName();
    }
  }

  private void clear() {
    this.mimeList.clear();
    this.mimeNameMap.clear();
    this.literalGlobMap.clear();
    this.extensionGlobMap.clear();
    this.otherGlobList.clear();
    this.maxTestBytes = 0;
    this.alwaysCheckMagic = false;
  }

  private void swap(final MimeRepository that) {
    assert (this != that);
    final List<MimeType> tempMimeList = that.mimeList;
    that.mimeList = this.mimeList;
    this.mimeList = tempMimeList;

    final Map<String, MimeType> tempMimeNameMap = that.mimeNameMap;
    that.mimeNameMap = this.mimeNameMap;
    this.mimeNameMap = tempMimeNameMap;

    Multimap<String, Pair<MimeGlob, MimeType>> tempGlobMap = that.literalGlobMap;
    that.literalGlobMap = this.literalGlobMap;
    this.literalGlobMap = tempGlobMap;

    tempGlobMap = that.extensionGlobMap;
    that.extensionGlobMap = this.extensionGlobMap;
    this.extensionGlobMap = tempGlobMap;

    final List<Pair<MimeGlob, MimeType>> tempGlobList = that.otherGlobList;
    that.otherGlobList = this.otherGlobList;
    this.otherGlobList = tempGlobList;

    final int tempMaxTestBytes = that.maxTestBytes;
    that.maxTestBytes = this.maxTestBytes;
    this.maxTestBytes = tempMaxTestBytes;

    final boolean tempAlwaysCheckMagic = that.alwaysCheckMagic;
    that.alwaysCheckMagic = this.alwaysCheckMagic;
    this.alwaysCheckMagic = tempAlwaysCheckMagic;
  }

  private void addMimeType(final MimeType mime) {
    // put the name to the m_mimeNameMap
    final String name = mime.getName();
    if (mimeNameMap.containsKey(name)) {
      logger.warn("The MIME-type name {} already existed. "
          + "The newer will override the older.", name);
    }
    logger.trace("Add the MIME-Type name: {}", name);
    mimeNameMap.put(name, mime);
    // add all aliases of the mime to the newMimeNameMap
    for (String alias : mime.getAliases()) {
      alias = alias.toLowerCase();
      if (mimeNameMap.containsKey(alias)) {
        logger.warn("The MIME-type alias {} already existed. "
            + "The newer will override the older.", alias);
      }
      logger.trace("Add the MIME-Type alias: {}", alias);
      mimeNameMap.put(alias, mime);
    }
    // add the glob pattern to the map
    for (final MimeGlob glob : mime.getGlobs()) {
      String pattern = glob.getPattern();
      // check whether the pattern is a special pattern
      if (Utils.isExtensionPattern(pattern)) {
        // found a extension pattern in the form of "*.ext"
        pattern = pattern.substring(2);
        logger.trace("Add a extension pattern: {}", pattern);
        extensionGlobMap.put(pattern, new Pair<>(glob, mime));
      } else if (Utils.isLiteralPattern(pattern)) {
        // found a literal pattern
        logger.trace("Add a literal pattern: {}", pattern);
        literalGlobMap.put(pattern, new Pair<>(glob, mime));
      } else {
        // add to the normal glob list
        logger.trace("Add a glob pattern: {}", pattern);
        otherGlobList.add(new Pair<>(glob, mime));
      }
    }
    // calculate the max test bytes
    for (final MimeMagic magic : mime.getMagics()) {
      // update the max test bytes
      final int bytes = magic.getMaxTestBytes();
      if (maxTestBytes < bytes) {
        logger.trace("Set the max test bytes to: {}", bytes);
        maxTestBytes = bytes;
      }
    }
  }

  public void fromXml(final Element root) throws XmlException {
    // first deserialize the XML and get a name map of MIME-types
    logger.trace("Deserialize MimeRepository from XML ...");
    DomUtils.checkNode(root, ROOT_NODE);
    final NodeList nodeList = root.getChildNodes();
    if (nodeList.getLength() == 0) {
      clear();
      return;
    }
    final MimeRepository temp = new MimeRepository();
    final int nodeCount = nodeList.getLength();
    for (int i = 0; i < nodeCount; ++i) {
      final Node node = nodeList.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) {
        continue; // skip non-element nodes
      }
      // deserialize a new MIME-type
      final MimeType mime = MimeTypeXmlSerializer.INSTANCE.deserialize((Element)node);
      logger.trace("Parsed a MIME-Type {}", mime);
      // add to the newMimeList
      temp.mimeList.add(mime);
      // collect glob information
      temp.addMimeType(mime);
    }
    // set the alwaysCheckMagic
    final Config config = MimeConfig.get();
    temp.alwaysCheckMagic = config.getBoolean(PROPERTY_CHECK_MAGIC, DEFAULT_CHECK_MAGIC);
    // then swap this with temp;
    swap(temp);
    logger.trace("Successfully deserialize MimeRepository from XML.");
  }

  public Element toXml(final Document doc) throws XmlException {
    logger.trace("Serializing MimeRepository into XML ...");
    final Element result = doc.createElement(ROOT_NODE);
    for (final MimeType mime : mimeList) {
      final Element node = MimeTypeXmlSerializer.INSTANCE.serialize(doc, mime);
      result.appendChild(node);
    }
    logger.trace("Successfully serialize MimeRepository into XML.");
    return result;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
               .append("mimeList", mimeList)
               .toString();
  }
}
