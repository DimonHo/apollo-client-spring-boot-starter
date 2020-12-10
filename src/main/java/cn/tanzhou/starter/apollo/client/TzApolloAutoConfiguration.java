package cn.tanzhou.starter.apollo.client;

import com.ctrip.framework.apollo.spring.config.ConfigPropertySourcesProcessor;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.ctrip.framework.apollo.spring.config.PropertySourcesProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TzApolloAutoConfiguration
 *
 * @author 敖癸
 * @date 2020/12/09 - 11:15
 */
@Configuration
@ConditionalOnProperty(value = PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED, havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(PropertySourcesProcessor.class)
public class TzApolloAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ConfigPropertySourcesProcessor.class)
    public ConfigPropertySourcesProcessor configPropertySourcesProcessor() {
        return new ConfigPropertySourcesProcessor();
    }
}
