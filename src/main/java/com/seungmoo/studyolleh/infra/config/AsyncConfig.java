package com.seungmoo.studyolleh.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 서비스 (Event Publisher 등)에 대해
 * 별도의 쓰레드 Executor를 등록해서 처리하자.
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 시스템의 processor 갯수를 불러오는 법
        int processors = Runtime.getRuntime().availableProcessors();
        log.info("processor count {}", processors);
        // 시스템의 프로세서 만큼 pool size를 설정한다.
        executor.setCorePoolSize(processors);
        // 맥스는 core size의 2배 정도로
        executor.setMaxPoolSize(processors);
        // queue capacity는 메모리 용량에 따라 별도로 설정한다.
        executor.setQueueCapacity(50);
        // 덤으로 더 만든 Pool 들을 얼마 동안 살려놓을 것인가 (MaxPoolSize)
        executor.setKeepAliveSeconds(60);
        // 비동기 프로세스는 별도로 prefix 지정
        executor.setThreadNamePrefix("AsyncExecutor-");
        // initialize까지 호출해서 리턴해준다.
        executor.initialize();
        return executor;
    }
}
