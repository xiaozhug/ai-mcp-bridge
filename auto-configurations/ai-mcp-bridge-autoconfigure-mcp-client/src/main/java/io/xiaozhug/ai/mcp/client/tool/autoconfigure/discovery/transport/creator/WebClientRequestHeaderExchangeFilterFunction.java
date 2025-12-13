package io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator;

import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * WebClient请求头交换过滤函数
 *
 * @author xiaozhug
 */
public class WebClientRequestHeaderExchangeFilterFunction implements ExchangeFilterFunction {

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        try {
            MultiValueMap<String, String> headers = RequestHeaderContextHolder.getHeaders();
            if (headers != null) {
                Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
                for(Map.Entry<String, List<String>> entry : entries){
                    String headerName = entry.getKey();
                    List<String> headerValues = entry.getValue();
                    request = ClientRequest.from(request)
                            .header(headerName, headerValues.toArray(new String[0]))
                            .build();
                }
            }

        } finally {
            RequestHeaderContextHolder.resetHeaders();
        }

        return next.exchange(request);
    }
}
