package io.xiaozhug.ai.mcp.metadata.file.expose.autoconfigure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.math.BigDecimal;

public final class JsonParser {

	private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
		.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
		.addModules(JacksonUtils.instantiateAvailableModules())
		.build();

	private JsonParser() {
	}

	/**
	 * Returns a Jackson {@link ObjectMapper} instance tailored for JSON-parsing
	 * operations for tool calling and structured output.
	 */
	public static ObjectMapper getObjectMapper() {
		return OBJECT_MAPPER;
	}

	/**
	 * Converts a JSON string to a Java object.
	 */
	public static <T> T fromJson(String json, Class<T> type) {
		Assert.notNull(json, "json cannot be null");
		Assert.notNull(type, "type cannot be null");

		try {
			return OBJECT_MAPPER.readValue(json, type);
		}
		catch (JsonProcessingException ex) {
			throw new IllegalStateException(String.format("Conversion from JSON to %s failed", type.getName()), ex);
		}
	}

	/**
	 * Checks if a string is a valid JSON string.
	 */
	private static boolean isValidJson(String input) {
		try {
			OBJECT_MAPPER.readTree(input);
			return true;
		}
		catch (JsonProcessingException e) {
			return false;
		}
	}

	/**
	 * Converts a Java object to a JSON string if it's not already a valid JSON string.
	 */
	public static String toJson(@Nullable Object object) {
		if (object instanceof String && isValidJson((String) object)) {
			return (String) object;
		}
		try {
			return OBJECT_MAPPER.writeValueAsString(object);
		}
		catch (JsonProcessingException ex) {
			throw new IllegalStateException("Conversion from Object to JSON failed", ex);
		}
	}

	/**
	 * Convert a Java Object to a typed Object. Based on the implementation in
	 * MethodToolCallback.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object toTypedObject(Object value, Class<?> type) {
		Assert.notNull(value, "value cannot be null");
		Assert.notNull(type, "type cannot be null");

		Class<?> javaType = ClassUtils.resolvePrimitiveIfNecessary(type);

		if (javaType == String.class) {
			return value.toString();
		}
		else if (javaType == Byte.class) {
			return Byte.parseByte(value.toString());
		}
		else if (javaType == Integer.class) {
			BigDecimal bigDecimal = new BigDecimal(value.toString());
			return bigDecimal.intValueExact();
		}
		else if (javaType == Short.class) {
			return Short.parseShort(value.toString());
		}
		else if (javaType == Long.class) {
			BigDecimal bigDecimal = new BigDecimal(value.toString());
			return bigDecimal.longValueExact();
		}
		else if (javaType == Double.class) {
			return Double.parseDouble(value.toString());
		}
		else if (javaType == Float.class) {
			return Float.parseFloat(value.toString());
		}
		else if (javaType == Boolean.class) {
			return Boolean.parseBoolean(value.toString());
		}
		else if (javaType.isEnum()) {
			return Enum.valueOf((Class<Enum>) javaType, value.toString());
		}

		Object result = null;
		if (value instanceof String) {
			try {
				result = JsonParser.fromJson((String) value, javaType);
			}
			catch (Exception e) {
				// ignore
			}
		}

		if (result == null) {
			String json = JsonParser.toJson(value);
			result = JsonParser.fromJson(json, javaType);
		}

		return result;
	}

}