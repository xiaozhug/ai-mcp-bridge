/*
 * Copyright 2012-2023 the original author or authors.
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

package org.springframework.boot.autoconfigure.condition;

import io.xiaozhug.ai.mcp.registry.autoconfigure.McpApplicationContextProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.*;
import java.util.Optional;

/**
 * Base of all {@link Condition} implementations used with Spring Boot. Provides sensible
 * logging to help the user diagnose what classes are loaded.
 *
 * @author Phillip Webb
 * @author Greg Turnquist
 * @author xiaozhug
 * @since 1.0.0
 */
@Slf4j
public abstract class SpringBootCondition implements Condition {

	private final Log logger = LogFactory.getLog(getClass());


	@Override
	public final boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		if (McpApplicationContextProcessor.mcpContextThreadLocal.get() && metadata.isAnnotated(ConditionalOnMissingBean.class.getName())) {
			log.info("Evaluating condition " + getClass().getSimpleName() + " for " + getName(metadata));

			MergedAnnotations annotations = metadata.getAnnotations();
			MergedAnnotation<ConditionalOnMissingBean> annotation = annotations.get(ConditionalOnMissingBean.class);
			SearchStrategy search = annotation.getValue("search", SearchStrategy.class).orElse(null);
			if(search != null && search == SearchStrategy.ALL){
				modifyAnnotationSearchStrategy(metadata);
            }
		}

		String classOrMethodName = getClassOrMethodName(metadata);
		try {
			ConditionOutcome outcome = getMatchOutcome(context, metadata);
			logOutcome(classOrMethodName, outcome);
			recordEvaluation(context, classOrMethodName, outcome);
			return outcome.isMatch();
		}
		catch (NoClassDefFoundError ex) {
			throw new IllegalStateException("Could not evaluate condition on " + classOrMethodName + " due to "
					+ ex.getMessage() + " not found. Make sure your own configuration does not rely on "
					+ "that class. This can also happen if you are "
					+ "@ComponentScanning a springframework package (e.g. if you "
					+ "put a @ComponentScan in the default package by mistake)", ex);
		}
		catch (RuntimeException ex) {
			throw new IllegalStateException("Error processing condition on " + getName(metadata), ex);
		}
	}

	private String getName(AnnotatedTypeMetadata metadata) {
		if (metadata instanceof AnnotationMetadata) {
			return ((AnnotationMetadata) metadata).getClassName();
		}
		if (metadata instanceof MethodMetadata) {
			MethodMetadata methodMetadata = (MethodMetadata) metadata;
			return methodMetadata.getDeclaringClassName() + "." + methodMetadata.getMethodName();
		}
		return metadata.toString();
	}

	private static String getClassOrMethodName(AnnotatedTypeMetadata metadata) {
		if (metadata instanceof ClassMetadata) {
			ClassMetadata classMetadata = (ClassMetadata) metadata;
			return classMetadata.getClassName();
		}
		MethodMetadata methodMetadata = (MethodMetadata) metadata;
		return methodMetadata.getDeclaringClassName() + "#" + methodMetadata.getMethodName();
	}

	protected final void logOutcome(String classOrMethodName, ConditionOutcome outcome) {
		if (this.logger.isTraceEnabled()) {
			this.logger.trace(getLogMessage(classOrMethodName, outcome));
		}
	}

	private StringBuilder getLogMessage(String classOrMethodName, ConditionOutcome outcome) {
		StringBuilder message = new StringBuilder();
		message.append("Condition ");
		message.append(ClassUtils.getShortName(getClass()));
		message.append(" on ");
		message.append(classOrMethodName);
		message.append(outcome.isMatch() ? " matched" : " did not match");
		if (StringUtils.hasLength(outcome.getMessage())) {
			message.append(" due to ");
			message.append(outcome.getMessage());
		}
		return message;
	}

	private void recordEvaluation(ConditionContext context, String classOrMethodName, ConditionOutcome outcome) {
		if (context.getBeanFactory() != null) {
			ConditionEvaluationReport.get(context.getBeanFactory())
				.recordConditionEvaluation(classOrMethodName, this, outcome);
		}
	}

	/**
	 * Determine the outcome of the match along with suitable log output.
	 * @param context the condition context
	 * @param metadata the annotation metadata
	 * @return the condition outcome
	 */
	public abstract ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata);

	/**
	 * Return true if any of the specified conditions match.
	 * @param context the context
	 * @param metadata the annotation meta-data
	 * @param conditions conditions to test
	 * @return {@code true} if any condition matches.
	 */
	protected final boolean anyMatches(ConditionContext context, AnnotatedTypeMetadata metadata,
			Condition... conditions) {
		for (Condition condition : conditions) {
			if (matches(context, metadata, condition)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return true if any of the specified condition matches.
	 * @param context the context
	 * @param metadata the annotation meta-data
	 * @param condition condition to test
	 * @return {@code true} if the condition matches.
	 */
	protected final boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata, Condition condition) {
		if (condition instanceof SpringBootCondition) {
			return ((SpringBootCondition) condition).getMatchOutcome(context, metadata).isMatch();
		}
		return condition.matches(context, metadata);
	}


	private void modifyAnnotationSearchStrategy(AnnotatedTypeMetadata metadata) {
		try {
			// 获取内部的 MergedAnnotations
			Field annotationsField = ReflectionUtils.findField(metadata.getClass(), "mergedAnnotations");
			if(annotationsField == null) {
				annotationsField = ReflectionUtils.findField(metadata.getClass(), "annotations");
			}
			annotationsField.setAccessible(true);
			MergedAnnotations originalAnnotations = (MergedAnnotations) annotationsField.get(metadata);

			// 使用动态代理包装 MergedAnnotations
			MergedAnnotations proxyAnnotations = (MergedAnnotations) Proxy.newProxyInstance(
					getClass().getClassLoader(),
					new Class[]{MergedAnnotations.class},
					new MergedAnnotationsInvocationHandler(originalAnnotations)
			);

			// 设置回去
			annotationsField.set(metadata, proxyAnnotations);

		} catch (Exception e) {
			log.warn("Failed to modify annotation search strategy", e);
		}
	}

	private static class MergedAnnotationsInvocationHandler implements InvocationHandler {
		private final MergedAnnotations delegate;

		public MergedAnnotationsInvocationHandler(MergedAnnotations delegate) {
			this.delegate = delegate;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Object result = method.invoke(delegate, args);

			if ("get".equals(method.getName()) && args.length > 0 &&
					(ConditionalOnMissingBean.class.equals(args[0]) ||
							ConditionalOnMissingBean.class.getName().equals(args[0]))) {

				// 包装返回的 MergedAnnotation
				return Proxy.newProxyInstance(
						getClass().getClassLoader(),
						new Class[]{MergedAnnotation.class},
						new MergedAnnotationInvocationHandler((MergedAnnotation<?>) result)
				);
			}

			return result;
		}
	}

	private static class MergedAnnotationInvocationHandler implements InvocationHandler {
		private final MergedAnnotation<?> delegate;

		public MergedAnnotationInvocationHandler(MergedAnnotation<?> delegate) {
			this.delegate = delegate;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ("getValue".equals(method.getName()) && args.length >= 2 &&
					"search".equals(args[0]) && SearchStrategy.class.equals(args[1])) {
				Optional<SearchStrategy> invoke = (Optional<SearchStrategy>) method.invoke(delegate, args);
				SearchStrategy result = invoke.orElseGet(null);
				return result == SearchStrategy.ALL ? Optional.of(SearchStrategy.CURRENT) : invoke;
			}

			return method.invoke(delegate, args);
		}
	}

}
