package com.readnshare.itemfinder.conifg;

import com.readnshare.itemfinder.interceptors.LogGrpcInterceptor;
import com.readnshare.itemfinder.interceptors.Oauth2AuthenticationInterceptor;
import com.readnshare.itemfinder.interceptors.SecurityContextPersistenceInterceptor;
import com.readnshare.itemfinder.security.AuthenticationManager;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class GlobalInterceptorConfiguration {

    @GrpcGlobalServerInterceptor
    public LogGrpcInterceptor logServerInterceptor() {
        return new LogGrpcInterceptor();
    }
//
//    @GrpcGlobalServerInterceptor
//    public SecurityContextPersistenceInterceptor securityContextPersistenceInterceptor() {
//        return new SecurityContextPersistenceInterceptor();
//    }
//
//    @GrpcGlobalServerInterceptor
//    public Oauth2AuthenticationInterceptor oauth2AuthenticationInterceptor(AuthenticationManager authenticationManager) {
//        return new Oauth2AuthenticationInterceptor(authenticationManager);
//    }
}
