package com.korea.MOVIEBOOK.member;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/profileimg/**")
                .addResourceLocations("file:///" + System.getProperty("user.dir") + "/Project2/" + "/profileimg/");
    }
}
