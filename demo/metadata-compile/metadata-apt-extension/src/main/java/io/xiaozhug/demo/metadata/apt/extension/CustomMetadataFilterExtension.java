package io.xiaozhug.demo.metadata.apt.extension;


import io.xiaozhug.ai.mcp.apt.extension.MetadataFilterExtension;

public class CustomMetadataFilterExtension implements MetadataFilterExtension {


    @Override
    public int getOrder() {
        return 0;
    }
}
