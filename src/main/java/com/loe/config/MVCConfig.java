package com.loe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MVCConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/graph");
        registry.addViewController("/graph").setViewName("graph");
        registry.addViewController("/plug").setViewName("plug");
	}
	
}