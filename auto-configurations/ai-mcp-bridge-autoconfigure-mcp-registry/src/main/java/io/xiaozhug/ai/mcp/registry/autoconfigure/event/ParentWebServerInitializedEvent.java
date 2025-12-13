package io.xiaozhug.ai.mcp.registry.autoconfigure.event;

import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationEvent;

/**
 * 父 Web 服务器初始化事件
 *
 * @author xiaozhug
 */
public abstract class ParentWebServerInitializedEvent extends ApplicationEvent {

    protected ParentWebServerInitializedEvent(WebServer webServer) {
        super(webServer);
    }

    public WebServer getWebServer() {
        return getSource();
    }

    public abstract WebServerApplicationContext getApplicationContext();

    @Override
    public WebServer getSource() {
        return (WebServer) super.getSource();
    }

}
