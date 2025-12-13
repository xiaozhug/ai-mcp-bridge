package io.xiaozhug.ai.mcp.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.xiaozhug.ai.mcp.common.metadata.McpMetadataItem;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class InputSchemaUtils {

    //填充描述信息
    public static void fillInputSchema(List<McpMetadataItem.Param> params, ObjectNode inputSchemaNodes) {

        if(!isEmpty(params) && inputSchemaNodes.has("properties")){
            JsonNode jsonNode = inputSchemaNodes.get("properties");
            JsonNode requiredJsonNode = inputSchemaNodes.get("required");
            int index = 0;
            for (Iterator<JsonNode> it = jsonNode.elements(); it.hasNext(); ) {
                ObjectNode element = (ObjectNode) it.next();
                McpMetadataItem.Param param = params.get(index++);
                if(param.isEnabled()){
                    element.put("description", param.getParamNameDescription());
                    List<McpMetadataItem.Param.Field> fields = param.getFields();
                    if(element.has("properties")){
                        setDescriptionsRecursively(element, element.get("properties"), fields);
                    }

                }else{
                    it.remove();
                    for (Iterator<JsonNode> rit = requiredJsonNode.elements(); rit.hasNext(); ) {
                        JsonNode rn = rit.next();
                        if(rn.asText().equals(param.getParamName())){
                            rit.remove();
                        }
                    }
                }
            }
        }
    }

    /**
     * 递归地为 JSON Schema 中的所有属性设置 description。
     *
     * @param parent 当前正在处理的 JSON Schema 节点的父节点。
     *
     * @param propertiesNode 当前正在处理的 JSON Schema 节点。它应该是一个包含 "properties" 的对象节点，
     *                   或者就是 "properties" 节点本身。
     * @param fieldList 与当前 schema 节点对应的元数据列表（可以是方法参数列表，也可以是对象的字段列表）。
     */
    private static void setDescriptionsRecursively(ObjectNode parent, JsonNode propertiesNode, List<McpMetadataItem.Param.Field> fieldList) {
        // 基本校验：确保节点是对象且包含 properties，并且元数据列表不为空
        if (isEmpty(fieldList)) {
            return;
        }

        // 遍历 properties 中的每个字段
        Iterator<Map.Entry<String, JsonNode>> fieldIterator = propertiesNode.fields();
        int index = 0; // 用于匹配元数据列表中的元素
        while (fieldIterator.hasNext() && index < fieldList.size()) {
            Map.Entry<String, JsonNode> entry = fieldIterator.next();
            String fieldName = entry.getKey();
            JsonNode fieldSchemaNode = entry.getValue();

            McpMetadataItem.Param.Field field = getField(fieldList, fieldName);

            // 找到对应的元数据后，设置 description
            if (fieldSchemaNode instanceof ObjectNode) {
                ObjectNode fieldSchemaObjectNode = (ObjectNode) fieldSchemaNode;

                try {
                    if(field.isEnabled()){
                        // 设置当前字段的 description
                        fieldSchemaObjectNode.put("description", field.getFieldNameDescription());

                        // --- 递归的核心在这里 ---
                        // 如果当前字段的 schema 本身又是一个对象 (type: object) 并且包含 properties，
                        // 那么我们就对它的 properties 进行同样的操作。
                        // 它的元数据就是当前 metadata 的 fields。
                        JsonNode typeJsonNode = fieldSchemaObjectNode.get("type");
                        JsonNode refJsonNode = fieldSchemaObjectNode.get("$ref");
                        if (typeJsonNode != null) {
                            switch (typeJsonNode.asText()) {
                                case "object":
                                    if (fieldSchemaObjectNode.has("properties")) {
                                        List<McpMetadataItem.Param.Field> nestedFieldsMetadata = field.getFields();
                                        setDescriptionsRecursively(fieldSchemaObjectNode, fieldSchemaObjectNode.get("properties"), nestedFieldsMetadata);
                                    }
                                    break;
                                case "array":
                                    JsonNode itemsNode = fieldSchemaObjectNode.get("items");
                                    if (itemsNode != null && itemsNode.isObject()) {
                                        ObjectNode itemsObjectNode = (ObjectNode) itemsNode;
                                        if (itemsObjectNode.has("properties")) {
                                            List<McpMetadataItem.Param.Field> nestedFieldsMetadata = field.getFields();
                                            if(nestedFieldsMetadata != null && !nestedFieldsMetadata.isEmpty()){
                                                setDescriptionsRecursively(itemsObjectNode, itemsObjectNode.get("properties"), nestedFieldsMetadata.get(0).getFields());
                                            }
                                        }
                                    }
                                    break;
                                default:
                                    // 其他类型不需要处理
                                    break;
                            }
                        } else if (refJsonNode != null) {
                            // 处理 $ref 引用的情况（如果需要）
                            JsonNode refNode = parent.at(refJsonNode.asText().replaceFirst("#",""));
                            if(refNode != null && refNode.has("properties")) {
                                List<McpMetadataItem.Param.Field> nestedFieldsMetadata = field.getFields();
                                setDescriptionsRecursively(parent, refNode.get("properties"), nestedFieldsMetadata);
                            }
                        }
                    } else {
                        // 如果字段被禁用，则从父节点中移除该字段
                        fieldIterator.remove();
                        if (parent.has("required")) {
                            JsonNode requiredNode = parent.get("required");
                            if (requiredNode.isArray()) {
                                Iterator<JsonNode> requiredIterator = requiredNode.elements();
                                while (requiredIterator.hasNext()) {
                                    JsonNode reqNode = requiredIterator.next();
                                    if (reqNode.asText().equals(fieldName)) {
                                        requiredIterator.remove();
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error setting description for field: " + fieldName, e);
                }
            }
        }
    }

    private static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    private static McpMetadataItem.Param.Field getField(List<McpMetadataItem.Param.Field> fields, String fieldName){
        if(!isEmpty(fields)){
            for(McpMetadataItem.Param.Field field : fields){
                if(field.getFieldName().equals(fieldName)){
                    return field;
                }
            }
        }
        return null;
    }
}
