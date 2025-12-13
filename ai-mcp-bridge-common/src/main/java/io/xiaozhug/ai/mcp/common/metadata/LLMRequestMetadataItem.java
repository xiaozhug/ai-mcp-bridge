package io.xiaozhug.ai.mcp.common.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 大语言模型请求元数据项
 *
 * @author xiaozhug
 */
@Getter
@Setter
public class LLMRequestMetadataItem {

    private String className;
    private String methodName;
    private String methodNameDescription;
    private List<Param> params;

    @Getter
    @Setter
    public static class Param {
        private String paramName;
        private String paramNameDescription;
        private String paramType;
        private List<Field> fields;

        @Getter
        @Setter
        public static class Field {
            private String fieldName;
            private String fieldNameDescription;
            private String fieldType;
            private List<Field> fields;
        }
    }
}
