package io.xiaozhug.ai.mcp.common.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * MCP元数据项
 *
 * @author xiaozhug
 */
@Getter
@Setter
public class McpMetadataItem {

    private String className;
    private String methodName;
    private String methodNameDescription;
    private List<Param> params;
    private RequestTemplateInfo requestTemplateInfo;
    private boolean enabled = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        McpMetadataItem that = (McpMetadataItem) o;
        return Objects.equals(className, that.className) && Objects.equals(methodName, that.methodName) && Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, params);
    }

    @Getter
    @Setter
    public static class Param{
        private String paramName;
        private String paramNameDescription;
        private String paramType;
        @JsonIgnore
        private String paramQualifiedName;
        private List<Field> fields;
        private boolean enabled = true;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Param param = (Param) o;
            return Objects.equals(paramName, param.paramName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(paramName);
        }

        @Getter
        @Setter
        public static class Field {
            private String fieldName;
            private String fieldNameDescription;
            private String fieldType;
            @JsonIgnore
            private String fieldQualifiedName;
            private List<Field> fields;
            private boolean enabled = true;
        }
    }

    public LLMRequestMetadataItem toLLMRequestMetadataItem(){
        LLMRequestMetadataItem item = new LLMRequestMetadataItem();
        item.setClassName(this.className);
        item.setMethodName(this.methodName);
        item.setMethodNameDescription(this.methodNameDescription);

        List<LLMRequestMetadataItem.Param> llmParam = new ArrayList<>();
        item.setParams(llmParam);
        if(this.params != null){
            for(Param param : this.params){
                LLMRequestMetadataItem.Param llmParamItem = new LLMRequestMetadataItem.Param();
                llmParamItem.setParamName(param.getParamName());
                llmParamItem.setParamNameDescription(param.getParamNameDescription());
                llmParamItem.setParamType(param.getParamType());

                List<LLMRequestMetadataItem.Param.Field> llmFields = new ArrayList<>();
                llmParamItem.setFields(llmFields);
                if(param.getFields() != null){
                    for(Param.Field field : param.getFields()){
                        LLMRequestMetadataItem.Param.Field llmFieldItem = new LLMRequestMetadataItem.Param.Field();
                        llmFieldItem.setFieldName(field.getFieldName());
                        llmFieldItem.setFieldNameDescription(field.getFieldNameDescription());
                        llmFieldItem.setFieldType(field.getFieldType());
                        llmFieldItem.setFields(field.getFields() != null ? toLLMFields(field.getFields()) : null);
                        llmFields.add(llmFieldItem);
                    }
                }
                llmParamItem.setFields(llmFields);
                llmParam.add(llmParamItem);
            }
        }

        return item;
    }

    private List<LLMRequestMetadataItem.Param.Field> toLLMFields(List<Param.Field> fields){
        List<LLMRequestMetadataItem.Param.Field> llmFields = new ArrayList<>();
        for(Param.Field field : fields){
            LLMRequestMetadataItem.Param.Field llmFieldItem = new LLMRequestMetadataItem.Param.Field();
            llmFieldItem.setFieldName(field.getFieldName());
            llmFieldItem.setFieldNameDescription(field.getFieldNameDescription());
            llmFieldItem.setFieldType(field.getFieldType());
            llmFieldItem.setFields(field.getFields() != null ? toLLMFields(field.getFields()) : null);
            llmFields.add(llmFieldItem);
        }
        return llmFields;
    }
}
