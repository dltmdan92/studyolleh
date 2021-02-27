package com.seungmoo.studyolleh.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    /**
     * Bcrypt Encoder를 쓰게 된다.
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        // underscore가 아닌 이상에는 하나의 property로 간주한다.
        // 디폴트 조건에서는 studyCreatedByEmail -> 이거를 그냥 대충 CamelCase로 해서 email에 넣을려고 한다.
        // underscore 조건만 넣어줌으로써 studyCreatedByEmail필드를 studyCreatedByEmail에 매핑될 수 있게 한다.
        modelMapper.getConfiguration()
                .setDestinationNameTokenizer(NameTokenizers.UNDERSCORE)
                .setSourceNameTokenizer(NameTokenizers.UNDERSCORE);
        return modelMapper;
    }

}
