package io.xiaozhug.ai.mcp.apt.extension;

import io.xiaozhug.ai.mcp.common.metadata.RequestTemplateInfo;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Map;

/**
 * 模板生成器扩展点接口
 * 允许自定义请求模板生成逻辑
 *
 * @author xiaozhug
 */
public interface TemplateGeneratorExtension extends ConfigurableExtension{

    /**
     * 在生成模板前调用，可以修改上下文
     */
    default Map<String, Object> beforeGenerateRequestTemplate(TypeElement classElement,
                                                              ExecutableElement methodElement,
                                                              Map<String, Object> context) {
        return context;
    }

    /**
     * 在生成模板后调用，可以修改模板
     */
    default void afterGenerateRequestTemplate(TypeElement classElement,
                                              ExecutableElement methodElement,
                                              RequestTemplateInfo templateInfo) {
    }
}