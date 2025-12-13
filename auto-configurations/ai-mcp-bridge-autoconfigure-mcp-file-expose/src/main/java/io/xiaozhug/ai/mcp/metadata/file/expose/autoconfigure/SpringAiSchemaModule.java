/*
 * Copyright 2023-2025 the original author or authors.
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

package io.xiaozhug.ai.mcp.metadata.file.expose.autoconfigure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.victools.jsonschema.generator.*;
import org.springframework.lang.Nullable;

import java.util.stream.Stream;

/**
 * JSON Schema Generator Module for Spring AI.
 * <p>
 * This module provides a set of customizations to the JSON Schema generator to support
 * the Spring AI framework. It allows to extract descriptions from
 * {@code @ToolParam(description = ...)} annotations and to determine whether a property
 * is required based on the presence of a series of annotations.
 *
 * @author Thomas Vitale
 * @since 1.0.0
 */
public final class SpringAiSchemaModule implements com.github.victools.jsonschema.generator.Module {

	private final boolean requiredByDefault;

	public SpringAiSchemaModule(Option... options) {
		this.requiredByDefault = Stream.of(options)
			.noneMatch(option -> option == Option.PROPERTY_REQUIRED_FALSE_BY_DEFAULT);
	}

	@Override
	public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
		this.applyToConfigBuilder(builder.forFields());
	}

	private void applyToConfigBuilder(SchemaGeneratorConfigPart<FieldScope> configPart) {
		configPart.withDescriptionResolver(this::resolveDescription);
		configPart.withRequiredCheck(this::checkRequired);
	}

	/**
	 * Extract description from {@code @ToolParam(description = ...)} for the given field.
	 */
	@Nullable
	private String resolveDescription(MemberScope<?, ?> member) {
		return null;
	}

	/**
	 * Determines whether a property is required based on the presence of a series of
	 * annotations.
	 * <p>
	 * <ul>
	 * <li>{@code @ToolParam(required = ...)}</li>
	 * <li>{@code @JsonProperty(required = ...)}</li>
	 * <li>{@code @Schema(required = ...)}</li>
	 * <li>{@code @Nullable}</li>
	 * </ul>
	 * <p>
	 * If none of these annotations are present, the default behavior is to consider the
	 * property as required, unless the {@link Option#PROPERTY_REQUIRED_FALSE_BY_DEFAULT}
	 * option is set.
	 */
	private boolean checkRequired(MemberScope<?, ?> member) {
		JsonProperty propertyAnnotation = member.getAnnotationConsideringFieldAndGetter(JsonProperty.class);
		if (propertyAnnotation != null) {
			return propertyAnnotation.required();
		}

		Nullable nullableAnnotation = member.getAnnotationConsideringFieldAndGetter(Nullable.class);
		if (nullableAnnotation != null) {
			return false;
		}

		return this.requiredByDefault;
	}

	/**
	 * Options for customizing the behavior of the module.
	 */
	public enum Option {

		/**
		 * Properties are only required if marked as such via one of the supported
		 * annotations.
		 */
		PROPERTY_REQUIRED_FALSE_BY_DEFAULT

	}

}
