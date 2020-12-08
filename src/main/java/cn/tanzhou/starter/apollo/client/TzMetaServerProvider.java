package cn.tanzhou.starter.apollo.client;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.spi.MetaServerProvider;
import com.ctrip.framework.foundation.Foundation;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

/**
 * 潭州apollo配置中心环境地址
 *
 * @author 敖癸
 * @date 2020/12/08 - 18:21
 */
@Slf4j
public class TzMetaServerProvider implements MetaServerProvider {

    public static final int ORDER = MetaServerProvider.LOWEST_PRECEDENCE;

    private final String metaServerAddress;

    public TzMetaServerProvider() {
        metaServerAddress = initMetaServerAddress();
    }

    private String initMetaServerAddress() {
        // 1. Get from System Property
        String env = System.getProperty(ConfigConsts.APOLLO_ENV_KEY);
        if (Strings.isNullOrEmpty(env)) {
            env = System.getProperty("spring.profiles.active");
        }
        String metaAddress = System.getProperty(ConfigConsts.APOLLO_META_KEY + env);
        if (Strings.isNullOrEmpty(metaAddress)) {
            // 2. Get from OS environment variable, which could not contain dot and is normally in UPPER case
            metaAddress = System.getenv("APOLLO_META");
        }
        if (Strings.isNullOrEmpty(metaAddress)) {
            // 3. Get from server.properties
            metaAddress = Foundation.server().getProperty(ConfigConsts.APOLLO_META_KEY, null);
        }
        if (Strings.isNullOrEmpty(metaAddress)) {
            // 4. Get from app.properties
            metaAddress = Foundation.app().getProperty(ConfigConsts.APOLLO_META_KEY, null);
        }
        if (Strings.isNullOrEmpty(metaAddress)) {
            // 4. Get from app.properties
            metaAddress = getTzMetaAddress(env);
        }
        if (Strings.isNullOrEmpty(metaAddress)) {
            log.warn(
                "Could not find meta server address, because it is not available in neither (1) JVM system property 'apollo.meta', (2) OS env variable 'APOLLO_META' (3) property 'apollo.meta' from server.properties nor (4) property 'apollo.meta' from app.properties");
        } else {
            metaAddress = metaAddress.trim();
            log.info("Located meta services from apollo.meta configuration: {}!", metaAddress);
        }

        return metaAddress;
    }

    private String getTzMetaAddress(String env) {
        switch (env) {
            case "test":
                return TzMetaAddress.TEST_META_ADDRESS;
            case "pre":
                return TzMetaAddress.PRE_META_ADDRESS;
            case "prod":
                return TzMetaAddress.PROD_META_ADDRESS;
            default:
                return TzMetaAddress.DEV_META_ADDRESS;
        }
    }

    @Override
    public String getMetaServerAddress(Env targetEnv) {
        //for default meta server provider, we don't care the actual environment
        return metaServerAddress;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
