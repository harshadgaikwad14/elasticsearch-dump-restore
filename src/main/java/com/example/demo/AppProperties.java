package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provides access to application configuration properties.
 */
@Component
public final class AppProperties {

	@Value("${elasticsearch.soruce.protocol}")
	private String esSoruceProtocol;

	@Value("${elasticsearch.soruce.hostname}")
	private String esSoruceHostname;

	@Value("${elasticsearch.soruce.port}")
	private String esSorucePort;

	@Value("${elasticsearch.soruce.user}")
	private String esSoruceUser;

	@Value("${elasticsearch.soruce.password}")
	private String esSorucePassword;

	@Value("${elasticsearch.destination.protocol}")
	private String esDestinationProtocol;

	@Value("${elasticsearch.destination.hostname}")
	private String esDestinationHostname;

	@Value("${elasticsearch.destination.port}")
	private String esDestinationPort;

	@Value("${elasticsearch.destination.user}")
	private String esDestinationUser;

	@Value("${elasticsearch.destination.password}")
	private String esDestinationPassword;

	/**
	 * Instantiates a new AppProperties object.
	 */
	private AppProperties() {
		// intentionally defined "private" to avoid others instantiating this class
	}

	public String getEsSoruceProtocol() {
		return esSoruceProtocol;
	}

	public String getEsSoruceHostname() {
		return esSoruceHostname;
	}

	public String getEsSorucePort() {
		return esSorucePort;
	}

	public String getEsSoruceUser() {
		return esSoruceUser;
	}

	public String getEsSorucePassword() {
		return esSorucePassword;
	}

	public String getEsDestinationProtocol() {
		return esDestinationProtocol;
	}

	public String getEsDestinationHostname() {
		return esDestinationHostname;
	}

	public String getEsDestinationPort() {
		return esDestinationPort;
	}

	public String getEsDestinationUser() {
		return esDestinationUser;
	}

	public String getEsDestinationPassword() {
		return esDestinationPassword;
	}

	public String getEsSourceEndpoint() {
		return getEsSoruceProtocol() + "://" + getEsSoruceHostname() + ":" + getEsSorucePort() + "/";
	}

	public String getEsDestinationEndpoint() {
		return getEsDestinationProtocol() + "://" + getEsDestinationHostname() + ":" + getEsDestinationPort() + "/";
	}

}
