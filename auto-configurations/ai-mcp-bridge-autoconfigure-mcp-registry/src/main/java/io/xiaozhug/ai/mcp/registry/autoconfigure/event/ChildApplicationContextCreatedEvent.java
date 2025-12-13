package io.xiaozhug.ai.mcp.registry.autoconfigure.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

/**
 * 子应用程序上下文创建事件
 *
 * @author xiaozhug
 */
public class ChildApplicationContextCreatedEvent extends ApplicationEvent {

    public ChildApplicationContextCreatedEvent(Object source) {
        super(source);
    }

    @Override
    public ApplicationContext getSource() {
        return (ApplicationContext) super.getSource();
    }
}
