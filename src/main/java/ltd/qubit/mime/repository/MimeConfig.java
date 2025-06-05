////////////////////////////////////////////////////////////////////////////////
//
//    Copyright (c) 2022 - 2024.
//    Haixing Hu, Qubit Co. Ltd.
//
//    All rights reserved.
//
////////////////////////////////////////////////////////////////////////////////
package ltd.qubit.mime.repository;

import ltd.qubit.commons.concurrent.Lazy;
import ltd.qubit.commons.config.Config;
import ltd.qubit.commons.config.error.ConfigurationError;

import static ltd.qubit.commons.config.ConfigUtils.loadXmlConfig;

/**
 * 提供获取mime-detect模块配置的函数，并定义属性的名称和默认值。
 *
 * @author 胡海星
 * @repository
 */
public class MimeConfig {

  /**
   * mime-detect模块配置的XML资源的系统属性名称。
   */
  public static final String PROPERTY_RESOURCE = "ltd.qubit.mime.repository.MimeConfig";

  /**
   * mime-detect模块配置的XML资源的默认名称。
   */
  public static final String DEFAULT_RESOURCE = "mime-detect.xml";

  /**
   * 静态 {@link Config} 对象的懒加载实例。
   */
  private static final Lazy<Config> lazyConfig = Lazy.of(() -> {
    final Config config = loadXmlConfig(PROPERTY_RESOURCE, DEFAULT_RESOURCE, MimeConfig.class);
    if (config.isEmpty()) {
      throw new ConfigurationError("Failed to load the configuration of mime-detect module.");
    }
    return config;
  });

  /**
   * 获取mime-detect模块的配置。
   * <p>
   * 该函数首先尝试在系统属性中查找配置的XML资源名称，如果不存在此类系统属性，
   * 将使用默认的XML资源。然后尝试从XML文件加载配置，如果成功则返回配置，
   * 如果失败则返回一个空配置。
   * </p>
   *
   * @return mime-detect模块的配置，如果失败则返回一个空配置。
   */
  public static Config get() {
    return lazyConfig.get();
  }
}