package com.example.demo;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

/**
 * Elasticsearch configuration.
 */
@Configuration
public class ElasticsearchConfig {

	/** Application properties. */
	@Autowired
	private AppProperties properties;

	/**
	 * Generates JestClient from factory .
	 *
	 * @return {@link JestClient} the jest client
	 */
	@Bean
	public JestClient getJestClient() {
		final JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig.Builder(properties.getEsDestinationProtocol() + "://"
				+ properties.getEsDestinationHostname() + ":" + properties.getEsDestinationPort())
						.defaultCredentials(properties.getEsDestinationUser(), properties.getEsDestinationPassword())
						.readTimeout(1500).connTimeout(1500).maxConnectionIdleTime(1500, TimeUnit.MILLISECONDS)
						.maxTotalConnection(10).multiThreaded(true).build());
		return factory.getObject();
	}

	/**
	 * ObjectMapper used for creation and mapping of json nodes.
	 *
	 * @return {@link ObjectMapper} the object mapper
	 */
	@Bean
	public ObjectMapper getObjectMapper() {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		final SimpleModule module = new SimpleModule();
		module.addSerializer(BigDecimal.class, new ToStringSerializer());
		mapper.registerModule(module);

		return mapper;
	}
}
