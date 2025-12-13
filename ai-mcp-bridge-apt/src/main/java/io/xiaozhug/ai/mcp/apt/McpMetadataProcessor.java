package io.xiaozhug.ai.mcp.apt;

import com.google.auto.service.AutoService;
import io.xiaozhug.ai.mcp.apt.collector.MetadataCollector;
import io.xiaozhug.ai.mcp.apt.config.ProcessorConfig;
import io.xiaozhug.ai.mcp.apt.extension.ExtensionLoader;
import io.xiaozhug.ai.mcp.apt.extension.ExtensionRegistry;
import io.xiaozhug.ai.mcp.apt.exception.ProcessorException;
import io.xiaozhug.ai.mcp.apt.explorer.ClassExplorer;
import io.xiaozhug.ai.mcp.apt.store.MetadataStore;
import io.xiaozhug.ai.mcp.apt.util.LogUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.*;

import static io.xiaozhug.ai.mcp.apt.McpMetadataProcessor.RestController;

/**
 * MCP元数据注解处理器
 * <p>
 * 主要功能：
 * 1. 扫描带有@RestController注解的类
 * 2. 提取方法信息和参数结构
 * 3. 调用大模型生成工具描述
 * 4. 生成MCP Server工具元数据文件
 * </p>
 *
 * @author xiaozhug
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes(RestController)
@SupportedOptions({
    ProcessorConfig.PARAM_PREFIX + "enabled",
    ProcessorConfig.PARAM_PREFIX + "chunk",
    ProcessorConfig.PARAM_PREFIX + "debug",
    ProcessorConfig.PARAM_PREFIX + "output",
    ProcessorConfig.PARAM_PREFIX + "targetPackages",
    ProcessorConfig.PARAM_PREFIX + "apiUrl",
    ProcessorConfig.PARAM_PREFIX + "apiKey",
    ProcessorConfig.PARAM_PREFIX + "model",
    ProcessorConfig.PARAM_PREFIX + "maxRetries",
    ProcessorConfig.PARAM_PREFIX + "extensions"
})
public class McpMetadataProcessor extends AbstractProcessor {

    public static final String RestController = "org.springframework.web.bind.annotation.RestController";

    private ProcessorConfig config;
    private MetadataStore metadataStore;
    private MetadataCollector metadataCollector;
    private ClassExplorer classExplorer;
    private ExtensionRegistry extensionRegistry;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        try {
            // 1. 初始化配置
            this.config = initConfig(processingEnv);

            if (!config.isEnabled()) {
                LogUtils.debug("注解处理器已禁用，跳过处理");
                return;
            }

            LogUtils.init(config);

            // 2. 加载扩展点
            this.extensionRegistry = ExtensionLoader.loadAllExtensions(config);

            // 3. 初始化组件
            this.metadataStore = new MetadataStore(processingEnv, config);
            this.metadataCollector = new MetadataCollector(
                processingEnv,
                metadataStore.readMetadata(),
                config,
                extensionRegistry
            );

            // 4. 初始化类处理器
            this.classExplorer = new ClassExplorer(processingEnv, config, extensionRegistry);

            LogUtils.debug(String.format("配置信息: 目标包=%s, 分片大小=%d, 调试模式=%s",
                config.getTargetPackages(), config.getChunkSize(), config.isDebugMode()));

        } catch (Exception e) {
            throw new ProcessorException("注解处理器初始化失败", e);
        }
    }

    /**
     * 初始化配置
     */
    private ProcessorConfig initConfig(ProcessingEnvironment processingEnv) {
        ProcessorConfig config = new ProcessorConfig();
        Map<String, String> options = processingEnv.getOptions();

        // 基本配置
        config.setEnabled(Boolean.parseBoolean(
            options.getOrDefault(ProcessorConfig.PARAM_PREFIX + "enabled",
            String.valueOf(ProcessorConfig.DEFAULT_ENABLED))));

        config.setChunkSize(Integer.parseInt(
            options.getOrDefault(ProcessorConfig.PARAM_PREFIX + "chunk",
            String.valueOf(ProcessorConfig.DEFAULT_CHUNK_SIZE))));

        config.setDebugMode(Boolean.parseBoolean(
            options.getOrDefault(ProcessorConfig.PARAM_PREFIX + "debug",
            String.valueOf(ProcessorConfig.DEFAULT_DEBUG_MODE))));

        config.setOutputPath(options.getOrDefault(
            ProcessorConfig.PARAM_PREFIX + "output",
            ProcessorConfig.DEFAULT_OUTPUT_PATH));

        config.setMaxRetries(Integer.parseInt(
            options.getOrDefault(ProcessorConfig.PARAM_PREFIX + "maxRetries",
            String.valueOf(ProcessorConfig.DEFAULT_MAX_RETRIES))));

        // 目标包配置
        String packagesStr = options.get(ProcessorConfig.PARAM_PREFIX + "targetPackages");
        if (packagesStr != null && !packagesStr.isEmpty()) {
            String[] packages = packagesStr.split(",");
            for (String pkg : packages) {
                pkg = pkg.trim();
                if (!pkg.isEmpty()) {
                    config.addTargetPackage(pkg);
                    LogUtils.debug("从配置获取目标包: " + pkg);
                }
            }
        }

        // LLM配置
        config.setApiUrl(options.get(ProcessorConfig.PARAM_PREFIX + "apiUrl"));
        config.setApiKey(options.get(ProcessorConfig.PARAM_PREFIX + "apiKey"));
        config.setModel(options.get(ProcessorConfig.PARAM_PREFIX + "model"));

        // 扩展点配置
        String extensionsStr = options.get(ProcessorConfig.PARAM_PREFIX + "extensions");
        if (extensionsStr != null && !extensionsStr.isEmpty()) {
            String[] extensionClasses = extensionsStr.split(",");
            for (String className : extensionClasses) {
                className = className.trim();
                if (!className.isEmpty()) {
                    config.getExtensionClasses().add(className);
                }
            }
        }

        return config;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!config.isEnabled() || !config.isValid()) {
            LogUtils.debug("配置无效或处理器已禁用，跳过处理");
            return false;
        }

        try {
            // 最后一轮：执行写入并清空数据
            if (roundEnv.processingOver()) {
                handleProcessingOver();
                return false;
            }

            // 非最后一轮：处理RestController类
            return processRestControllerClasses(roundEnv);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ProcessorException("注解处理失败", e);
        }
    }

    /**
     * 处理RestController类
     */
    private boolean processRestControllerClasses(RoundEnvironment roundEnv) {
        TypeElement restControllerElement = processingEnv.getElementUtils()
            .getTypeElement(RestController);

        if (restControllerElement == null) {
            LogUtils.debug("未找到@RestController注解定义");
            return false;
        }

        Set<? extends Element> restControllerClasses =
            roundEnv.getElementsAnnotatedWith(restControllerElement);

        if (restControllerClasses.isEmpty()) {
            LogUtils.debug("未找到带有@RestController注解的类");
            return false;
        }

        LogUtils.debug(String.format("找到 %d 个RestController类", restControllerClasses.size()));

        // 处理每个RestController类
        int processedCount = 0;
        for (Element clazz : restControllerClasses) {
            if (clazz.getKind() != ElementKind.CLASS) {
                continue;
            }

            try {
                boolean processed = classExplorer.processClass((TypeElement) clazz, metadataCollector, processedCount);

                if (processed) {
                    processedCount++;

                    // 分片处理逻辑
                    if (processedCount % config.getChunkSize() == 0) {
                        metadataCollector.generateMetadata();
                    }
                }
            } catch (Exception e) {
                throw new ProcessorException("处理类失败: " + clazz, e);
            }
        }

        // 处理剩余未分片的元数据
        if (processedCount % config.getChunkSize() != 0) {
            metadataCollector.generateMetadata();
        }

        LogUtils.debug(String.format("处理完成，共处理 %d 个类", processedCount));
        return false;
    }

    /**
     * 处理结束时的逻辑
     */
    private void handleProcessingOver() throws Exception {
        LogUtils.debug("处理结束，开始写入元数据...");

        // 合并并写入元数据
        metadataCollector.writeMetadata(metadataStore);

        // 清空当前轮次的数据
        metadataCollector.clearCurrentMetadatas();

        LogUtils.debug("元数据写入完成");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }
}