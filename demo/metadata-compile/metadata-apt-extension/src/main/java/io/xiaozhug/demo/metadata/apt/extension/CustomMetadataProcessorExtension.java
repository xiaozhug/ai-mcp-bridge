package io.xiaozhug.demo.metadata.apt.extension;


import io.xiaozhug.ai.mcp.apt.extension.MetadataProcessorExtension;

public class CustomMetadataProcessorExtension implements MetadataProcessorExtension {


    @Override
    public int getOrder() {
        return 0;
    }
}
