package com.matrix.apigateway.config;

import com.matrix.apigateway.util.Aes256;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class EncryptResponse implements GlobalFilter, Ordered {


	@Value("${AES256_SALT_BODY}")
	private String saltBody;

	@Value("${AES256_SECRET_KEY_BODY}")
	private String secretKeyBody;


	@Override
	public int getOrder() {
		return -2;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		ServerHttpResponse originalResponse = exchange.getResponse();
		DataBufferFactory bufferFactory = originalResponse.bufferFactory();
		ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            	if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.map(dataBuffer -> {
						byte[] content = new byte[dataBuffer.readableByteCount()];
						dataBuffer.read(content);
						Aes256 aes256 = new Aes256(saltBody,secretKeyBody);
						String encryptedContent = aes256.encrypt(new String(content));
						byte[] encryptedBytes = encryptedContent.getBytes();
						return bufferFactory.wrap(encryptedBytes);
                    }));
            	}
            	return super.writeWith(body);
            }			
		};
		return chain.filter(exchange.mutate().response(decoratedResponse).build());
	}
}