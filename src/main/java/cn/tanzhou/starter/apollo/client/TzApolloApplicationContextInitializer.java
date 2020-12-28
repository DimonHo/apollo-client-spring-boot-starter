package cn.tanzhou.starter.apollo.client;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.ctrip.framework.apollo.util.factory.PropertiesFactory;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 重写ApolloApplicationContextInitializer <br />
 * 兼容spring.application.name和spring.profiles.active属性 <br />
 * 默认启动dev环境，根据环境自动加载潭州注册中心地址
 *
 * @author 敖癸
 * @date 2020/12/8 - 21:05
 */
@Slf4j
public class TzApolloApplicationContextInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext>, EnvironmentPostProcessor, Ordered {

    public static final int DEFAULT_ORDER = 0;

    private static final Splitter NAMESPACE_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
    private static final String[] APOLLO_SYSTEM_PROPERTIES = {ConfigConsts.APOLLO_ID_KEY, ConfigConsts.APOLLO_CLUSTER_KEY,
        ConfigConsts.APOLLO_CACHE_DIR_KEY, ConfigConsts.APOLLO_ACCESSKEY_SECRET_KEY, ConfigConsts.APOLLO_ENV_KEY,
        PropertiesFactory.APOLLO_PROPERTY_ORDER_ENABLE};

    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector
        .getInstance(ConfigPropertySourceFactory.class);

    private int order = DEFAULT_ORDER;

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();

        if (!environment.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED, Boolean.class, true)) {
            log.debug("Apollo bootstrap config is not enabled for context {}, see property: ${{}}", context,
                PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED);
            return;
        }
        log.debug("Apollo bootstrap config is enabled for context {}", context);

        initialize(environment);
    }


    /**
     * Initialize Apollo Configurations Just after environment is ready.
     *
     * @param environment
     */
    protected void initialize(ConfigurableEnvironment environment) {

        if (environment.getPropertySources().contains(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            //already initialized
            return;
        }

        String namespaces = environment
            .getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_NAMESPACES, ConfigConsts.NAMESPACE_APPLICATION);
        log.debug("Apollo bootstrap namespaces: {}", namespaces);
        List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(namespaces);

        CompositePropertySource composite = new CompositePropertySource(
            PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
        for (String namespace : namespaceList) {
            Config config = ConfigService.getConfig(namespace);

            composite.addPropertySource(configPropertySourceFactory.getConfigPropertySource(namespace, config));
        }

        environment.getPropertySources().addFirst(composite);
    }

    /**
     * To fill system properties from environment config
     */
    void initializeSystemProperty(ConfigurableEnvironment environment) {
        for (String propertyName : APOLLO_SYSTEM_PROPERTIES) {
            fillSystemPropertyFromEnvironment(environment, propertyName);
            if (ConfigConsts.APOLLO_ENV_KEY.equals(propertyName)) {
                fillSystemPropertyFromEnvironment(environment, ConfigConsts.APOLLO_META_KEY + System.getProperty(propertyName));
            }
        }
    }

    private void fillSystemPropertyFromEnvironment(ConfigurableEnvironment environment, String propertyName) {
        String propertyValue = environment.getProperty(propertyName);
        // 如果app.id未指定，则获取spring.application.name的值
        if (ConfigConsts.APOLLO_ID_KEY.equals(propertyName) && Strings.isNullOrEmpty(propertyValue)) {
            propertyValue = environment.getProperty(ConfigConsts.SPRING_APPLICATION_NAME_KEY);
        }
        // 如果app.env未指定，则获取spring.profiles.active的值
        if (ConfigConsts.APOLLO_ENV_KEY.equals(propertyName) && Strings.isNullOrEmpty(propertyValue)) {
            propertyValue = environment.getProperty(ConfigConsts.SPRING_PROFILES_ACTIVE_KEY);
            // 如果 app.env和spring.profiles.active都未指定，默认加载dev环境配置
            if (Strings.isNullOrEmpty(propertyValue)) {
                propertyValue = TzMetaAddress.DEFAULT_ENV;
            }
        }
        if (StringUtils.startsWith(propertyName, ConfigConsts.APOLLO_META_KEY)) {
            if (Strings.isNullOrEmpty(propertyValue)) {
                // 自动加载注册中心地址，如果没找到相应的环境，默认注册到dev环境
                propertyValue = TzMetaAddress.getTzMetaAddress(System.getProperty(ConfigConsts.APOLLO_ENV_KEY));
            }
        }

        if (System.getProperty(propertyName) != null || Strings.isNullOrEmpty(propertyValue)) {
            return;
        }

        System.setProperty(propertyName, propertyValue);
    }

    /**
     * In order to load Apollo configurations as early as even before Spring loading logging system phase, this
     * EnvironmentPostProcessor can be called Just After ConfigFileApplicationListener has succeeded.
     * <p>
     * <br /> The processing sequence would be like this: <br /> Load Bootstrap properties and application properties ----->
     * load Apollo configuration properties ----> Initialize Logging systems
     *
     * @param configurableEnvironment
     * @param springApplication
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment configurableEnvironment, SpringApplication springApplication) {

        // should always initialize system properties like app.id in the first place
        initializeSystemProperty(configurableEnvironment);

        Boolean eagerLoadEnabled = configurableEnvironment
            .getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_EAGER_LOAD_ENABLED, Boolean.class, false);

        //EnvironmentPostProcessor should not be triggered if you don't want Apollo Loading before Logging System Initialization
        if (!eagerLoadEnabled) {
            return;
        }

        Boolean bootstrapEnabled = configurableEnvironment
            .getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED, Boolean.class, false);

        if (bootstrapEnabled) {
            initialize(configurableEnvironment);
        }

    }

    /**
     * @since 1.3.0
     */
    @Override
    public int getOrder() {
        return order;
    }

    /**
     * @since 1.3.0
     */
    public void setOrder(int order) {
        this.order = order;
    }
}

