package com.readnshare.itemfinder.conifg;

import com.readnshare.itemfinder.interceptors.LogGrpcInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class GlobalInterceptorConfiguration {

    @GrpcGlobalServerInterceptor
    public LogGrpcInterceptor logServerInterceptor() {
        return new LogGrpcInterceptor();
    }
}
