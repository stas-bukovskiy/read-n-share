package com.readnshare.itemfinder.conifg;

import com.readnshare.itemfinder.interceptors.LogGrpcInterceptor;
import com.readnshare.itemfinder.interceptors.TokenValidationInterceptor;
import com.readnshare.itemfinder.services.VerifyUserService;
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
