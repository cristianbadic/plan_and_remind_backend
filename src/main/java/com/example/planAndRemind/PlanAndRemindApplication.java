package com.example.planAndRemind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ClickSend.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlanAndRemindApplication {

	@Value("${clickSend-username}")
	private String clickSendUsername;
	@Value("${clickSend-apiKey}")
	private String clickSendApiKey;

	@Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
	public ApiClient clickSendConfig(){
		ApiClient clickSendApiClient = new ApiClient();
		clickSendApiClient.setUsername(clickSendUsername);
		clickSendApiClient.setPassword(clickSendApiKey);
		return clickSendApiClient;
	}

	public static void main(String[] args) {
		SpringApplication.run(PlanAndRemindApplication.class, args);
	}

}
