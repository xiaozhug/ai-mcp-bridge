package io.xiaozhug.demo.metadata.apt.extension;


import io.xiaozhug.ai.mcp.apt.extension.TemplateGeneratorExtension;

public class CustomTemplateGeneratorExtension implements TemplateGeneratorExtension {


    @Override
    public int getOrder() {
        return 0;
    }
}
