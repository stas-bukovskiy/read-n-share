package com.readnshare.itemshelfer.conifg;

import com.readnshare.itemshelfer.interceptors.LogGrpcInterceptor;
import com.readnshare.itemshelfer.interceptors.TokenValidationInterceptor;
import com.readnshare.itemshelfer.services.VerifyUserService;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class GlobalInterceptorConfiguration {

    @Bean
    @GrpcGlobalServerInterceptor
    public LogGrpcInterceptor logServerInterceptor() {
        return new LogGrpcInterceptor();
    }

    @Bean
    @GrpcGlobalServerInterceptor
    public TokenValidationInterceptor tokenValidationInterceptor(VerifyUserService verifyUserService) {
        return new TokenValidationInterceptor(verifyUserService);
    }
}
