////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import ltd.qubit.commons.config.Config;
import ltd.qubit.commons.config.error.ConfigurationError;

import static ltd.qubit.commons.config.ConfigUtils.loadXmlConfig;

/**
 * Provides functions to get the configuration of the mime-detect module and
 * defines the names and default values of properties.
 *
 * @author Haixing Hu
 */
public class MimeConfig {

  /**
   * The system property name for the XML resource of the configuration of
   * mime-detect module.
   */
  public static final String PROPERTY_RESOURCE = "ltd.qubit.mime.repository.MimeConfig";

  /**
   * The default name of XML resource of the configuration of mime-detect module.
   */
  public static final String DEFAULT_RESOURCE = "mime-detect.xml";

  /**
   * The static {@link Config} object.
   */
  private static volatile Config config = null;

  /**
   * Gets the configuration of the mime-detect module.
   * <p>
   * The function will first try to search in the system's properties to find
   * the XML resource name of the configuration, if no such system properties
   * exists, it will use the default XML resource. Then it will try to load the
   * configuration from the XML file, and return the configuration if success,
   * or return an empty configuration if failed.
   * </p>
   *
   * @return the configuration of the mime-detect module, or an empty
   *         configuration if failed.
   */
  public static Config get() {
    // use the double check locking
    if (config == null) {
      synchronized (MimeConfig.class) {
        if (config == null) {
          config = loadXmlConfig(PROPERTY_RESOURCE, DEFAULT_RESOURCE, MimeConfig.class);
          if (config.isEmpty()) {
            throw new ConfigurationError("Failed to load the configuration of mime-detect module.");
          }
        }
      }
    }
    return config;
  }
}
