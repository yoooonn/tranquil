package com.ycourlee.tranquil.autoconfiguration.redisson;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.boot.autoconfigure.PebbleAutoConfiguration;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.loader.StringLoader;
import com.ycourlee.tranquil.redisson.RedissonTemplate;
import com.ycourlee.tranquil.redisson.annotation.Lockable;
import com.ycourlee.tranquil.redisson.aop.LockableAnnotationAdvisor;
import com.ycourlee.tranquil.redisson.aop.LockableMethodInterceptor;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author yooonn
 * @date 2022.04.14
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean({RedissonClient.class})
@AutoConfigureAfter({RedissonAutoConfiguration.class})
@ConditionalOnProperty(prefix = "tranquil.redisson", name = "enable", havingValue = "true", matchIfMissing = true)
public class RedissonAnnotationAutoConfiguration {

    /**
     * Override loader bean of {@link PebbleAutoConfiguration#pebbleLoader(com.mitchellbosecke.pebble.boot.autoconfigure.PebbleProperties)}
     * Should allow override bean. via {@code spring.main.allow-bean-definition-overriding=true}
     *
     * @return pebble string loader
     */
    @Bean("pebbleLoader")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnClass(PebbleEngine.class)
    public Loader<?> pebbleLoader() {
        // fake setters appeared in string loader
        return new StringLoader();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean
    public RedissonTemplate tranquilRedissonTemplate(
            @Qualifier("redisson") RedissonClient redissonClient
    ) {
        return new RedissonTemplate(redissonClient);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(name = "tranquilRedissonDefaultPointcut")
    public Pointcut tranquilRedissonDefaultPointcut() {
        return new AnnotationMatchingPointcut(null, Lockable.class, true);
    }

    /**
     * 默认使用pebble生成key. 当然, 也可以继承{@link LockableMethodInterceptor}
     * 重写其中生成key的方法, 再override这个bean
     *
     * @param redissonTemplate redisson template
     * @return lockable interceptor
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean
    public LockableMethodInterceptor tranquilLockableMethodInterceptor(
            RedissonTemplate redissonTemplate
    ) {
        return new LockableMethodInterceptor(redissonTemplate);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean
    public LockableAnnotationAdvisor lockableAnnotationAdvisor(
            LockableMethodInterceptor interceptor,
            @Qualifier("tranquilRedissonDefaultPointcut") Pointcut pointcut
    ) {
        return new LockableAnnotationAdvisor(interceptor, pointcut);
    }
}
