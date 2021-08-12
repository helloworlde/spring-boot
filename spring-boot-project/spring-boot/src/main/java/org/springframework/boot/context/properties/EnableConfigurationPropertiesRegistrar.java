/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.context.properties;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.validation.beanvalidation.MethodValidationExcludeFilter;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Conventions;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotationMetadata;

/**
 * {@link ImportBeanDefinitionRegistrar} for
 * {@link EnableConfigurationProperties @EnableConfigurationProperties}.
 *
 * 用于注册 @EnableConfigurationProperties 标记的 Bean
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
class EnableConfigurationPropertiesRegistrar implements ImportBeanDefinitionRegistrar {

	private static final String METHOD_VALIDATION_EXCLUDE_FILTER_BEAN_NAME = Conventions
			.getQualifiedAttributeName(EnableConfigurationPropertiesRegistrar.class, "methodValidationExcludeFilter");

	/**
	 * 注册 bean 定义
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		// 注册 ConfigurationPropertiesBindingPostProcessor Bean 和 BoundConfigurationProperties Bean
		registerInfrastructureBeans(registry);
		// 注册 MethodValidationExcludeFilter Bean
		registerMethodValidationExcludeFilter(registry);
		// 创建注册器
		ConfigurationPropertiesBeanRegistrar beanRegistrar = new ConfigurationPropertiesBeanRegistrar(registry);
		// 获取 EnableConfigurationProperties 注解中指定的类，依次注册相应的 Bean
		getTypes(metadata).forEach(beanRegistrar::register);
	}

	/**
	 * 获取 EnableConfigurationProperties 注解指定的类型
	 */
	private Set<Class<?>> getTypes(AnnotationMetadata metadata) {
		return metadata.getAnnotations()
		               // 过滤只保留指定的注解
		               .stream(EnableConfigurationProperties.class)
				.flatMap((annotation) -> Arrays.stream(annotation.getClassArray(MergedAnnotation.VALUE)))
				.filter((type) -> void.class != type).collect(Collectors.toSet());
	}

	/**
	 * 注册 ConfigurationPropertiesBindingPostProcessor Bean 和 BoundConfigurationProperties Bean
	 */
	static void registerInfrastructureBeans(BeanDefinitionRegistry registry) {
		ConfigurationPropertiesBindingPostProcessor.register(registry);
		BoundConfigurationProperties.register(registry);
	}

	/**
	 * 注册 MethodValidationExcludeFilter Bean
	 */
	static void registerMethodValidationExcludeFilter(BeanDefinitionRegistry registry) {
		if (!registry.containsBeanDefinition(METHOD_VALIDATION_EXCLUDE_FILTER_BEAN_NAME)) {
			BeanDefinition definition = BeanDefinitionBuilder
					.genericBeanDefinition(MethodValidationExcludeFilter.class,
							() -> MethodValidationExcludeFilter.byAnnotation(ConfigurationProperties.class))
					.setRole(BeanDefinition.ROLE_INFRASTRUCTURE).getBeanDefinition();
			registry.registerBeanDefinition(METHOD_VALIDATION_EXCLUDE_FILTER_BEAN_NAME, definition);
		}
	}

}
