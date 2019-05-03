package com.example.demo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Update;

@Service
public class EsService {

	@Autowired
	private AppProperties appProperties;

	/** Elasticsearch Jest client. */
	@Autowired
	private JestClient jestClient;

	/** Object Mapper object. */
	@Autowired
	public ObjectMapper objectMapper;

	public JsonNode getSoruceEsMapping(final String esIndexName) throws JsonProcessingException, IOException {

		final String esSoruceUrl = appProperties.getEsSourceEndpoint() + esIndexName + "/";
		// HttpHeaders
		HttpHeaders headers = new HttpHeaders();

		String auth = appProperties.getEsSoruceUser() + ":" + appProperties.getEsSorucePassword();
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
		String authHeader = "Basic " + new String(encodedAuth);
		headers.set("Authorization", authHeader);
		//
		headers.setAccept(Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON }));
		// Request to return JSON format
		headers.setContentType(MediaType.APPLICATION_JSON);

		// HttpEntity<String>: To get result as String.
		HttpEntity<String> entity = new HttpEntity<String>(headers);

		// RestTemplate
		RestTemplate restTemplate = new RestTemplate();

		// Send request with GET method, and Headers.
		ResponseEntity<String> response = restTemplate.exchange(esSoruceUrl, HttpMethod.GET, entity, String.class);

		String result = response.getBody();

		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode actualObj = objectMapper.readTree(result);

		ObjectNode object = (ObjectNode) actualObj;

		if (object.has(esIndexName)) {

			JsonNode indexNameNode = object.get(esIndexName);
			System.out.println("indexNameNode : " + indexNameNode);
			if (indexNameNode.has("settings")) {
				JsonNode settingsNode = indexNameNode.get("settings");
				System.out.println("settingsNode : " + settingsNode);
				if (settingsNode.has("index")) {

					JsonNode indexNode = settingsNode.get("index");
					System.out.println("indexNode : " + indexNode);
					if (indexNode.has("provided_name")) {
						((ObjectNode) indexNode).remove("provided_name");
					}
					if (indexNode.has("creation_date")) {
						((ObjectNode) indexNode).remove("creation_date");
					}
					if (indexNode.has("uuid")) {
						((ObjectNode) indexNode).remove("uuid");
					}
					if (indexNode.has("version")) {
						((ObjectNode) indexNode).remove("version");
					}
				}
			}

		}

		return actualObj.get(esIndexName);
	}

	public void createEsMapping(final String esIndexName, final JsonNode jsonNode) {

		System.out.println("createEsMapping esIndexName : " + esIndexName);
		System.out.println("createEsMapping jsonNode : " + jsonNode);

		final String esDestinationUrl = appProperties.getEsDestinationEndpoint() + esIndexName + "/";

		HttpHeaders headers = new HttpHeaders();
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

		if ((appProperties.getEsDestinationUser() != null && !appProperties.getEsDestinationUser().isEmpty())
				&& (appProperties.getEsDestinationPassword() != null
						&& !appProperties.getEsDestinationPassword().isEmpty())) {

			String auth = appProperties.getEsSoruceUser() + ":" + appProperties.getEsSorucePassword();
			byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
			String authHeader = "Basic " + new String(encodedAuth);
			headers.set("Authorization", authHeader);
		}

		RestTemplate restTemplate = new RestTemplate();

		// Data attached to the request.
		HttpEntity<JsonNode> requestBody = new HttpEntity<>(jsonNode, headers);

		// Send request with PUT method.
		restTemplate.put(esDestinationUrl, requestBody, new Object[] {});

	}

	public JsonNode getEsTypeData(final String esIndexName, final String esType)
			throws JsonProcessingException, IOException, InterruptedException {

		// HttpHeaders
		HttpHeaders headers = new HttpHeaders();

		JsonNode actualObj = null;

		final String esSoruceUrl = appProperties.getEsSourceEndpoint() + esIndexName + "/" + esType
				+ "/_search?size=10000";

		try {

			String auth = appProperties.getEsSoruceUser() + ":" + appProperties.getEsSorucePassword();
			byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
			String authHeader = "Basic " + new String(encodedAuth);
			headers.set("Authorization", authHeader);
			//
			headers.setAccept(Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON }));
			// Request to return JSON format
			headers.setContentType(MediaType.APPLICATION_JSON);

			// HttpEntity<String>: To get result as String.
			HttpEntity<String> entity = new HttpEntity<String>(headers);

			// RestTemplate
			RestTemplate restTemplate = new RestTemplate();

			// Send request with GET method, and Headers.
			ResponseEntity<String> response = restTemplate.exchange(esSoruceUrl, HttpMethod.GET, entity, String.class);

			String result = response.getBody();

			// System.out.println(result);
			ObjectMapper objectMapper = new ObjectMapper();

			actualObj = objectMapper.readTree(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return actualObj;

	}

	public void updateEsData(final String esIndexName, final JsonNode actualObj) throws InterruptedException {

		try {

			ObjectNode object = (ObjectNode) actualObj;

			if (object.has("hits")) {

				JsonNode hitsNode = object.get("hits");

				if (hitsNode.has("hits")) {
					JsonNode hitsArrayNode = hitsNode.get("hits");

					if (hitsArrayNode.isArray()) {

						long count = hitsArrayNode.size();

						System.out.println("Total Record : " + count);

						for (JsonNode hitsArrayNodeData : hitsArrayNode) {

							if (count % 25 == 0) {
								Thread.sleep(100);
							}

							JsonNode _sourceNode = null;
							String _type = null;
							String _id = null;
							if (hitsArrayNodeData.has("_type")) {
								_type = hitsArrayNodeData.get("_type").asText();
							}
							if (hitsArrayNodeData.has("_id")) {
								_id = hitsArrayNodeData.get("_id").asText();
							}

							if (hitsArrayNodeData.has("_source")) {

								_sourceNode = hitsArrayNodeData.get("_source");

							}

							final ObjectNode docNode = JsonNodeFactory.instance.objectNode();

							docNode.set("doc", _sourceNode);

							docNode.set("doc_as_upsert", objectMapper.convertValue(true, JsonNode.class));
							final Update update = new Update.Builder(docNode.toString()).index(esIndexName).type(_type)
									.id(_id).build();
							JestResult result = jestClient.execute(update);

							System.out.println("Response  : " + result.getJsonString());

							System.out.println("Data Restored Successfully : type :" + _type + " id : " + _id);
							count--;
							System.out.println("Remaining Record to restore : " + count);

						}

					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public JsonNode getEsTypeSpecificData(final String esIndexName, final String esType, String _id)
			throws JsonProcessingException, IOException, InterruptedException {

		// HttpHeaders
		HttpHeaders headers = new HttpHeaders();

		JsonNode actualObj = null;

		if (_id.contains("#")) {
			_id = _id.replace("#", "%23");
		}

		final String esSoruceUrl = appProperties.getEsSourceEndpoint() + esIndexName + "/" + esType + "/" + _id;

		try {

			String auth = appProperties.getEsSoruceUser() + ":" + appProperties.getEsSorucePassword();
			byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
			String authHeader = "Basic " + new String(encodedAuth);
			headers.set("Authorization", authHeader);
			//
			headers.setAccept(Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON }));
			// Request to return JSON format
			headers.setContentType(MediaType.APPLICATION_JSON);

			// HttpEntity<String>: To get result as String.
			HttpEntity<String> entity = new HttpEntity<String>(headers);

			// RestTemplate
			RestTemplate restTemplate = new RestTemplate();

			// Send request with GET method, and Headers.
			ResponseEntity<String> response = restTemplate.exchange(esSoruceUrl, HttpMethod.GET, entity, String.class);

			String result = response.getBody();

			// System.out.println(result);
			ObjectMapper objectMapper = new ObjectMapper();

			actualObj = objectMapper.readTree(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return actualObj;

	}

	public void updateEsDataSpecific(final String esIndexName, final JsonNode actualObj) throws InterruptedException {

		try {

			ObjectNode object = (ObjectNode) actualObj;

			JsonNode _sourceNode = null;
			String _type = null;
			String _id = null;
			if (object.has("_type")) {
				_type = object.get("_type").asText();
			}
			if (object.has("_id")) {
				_id = object.get("_id").asText();
			}

			if (object.has("_source")) {

				_sourceNode = object.get("_source");

			}

			final ObjectNode docNode = JsonNodeFactory.instance.objectNode();

			docNode.set("doc", _sourceNode);

			docNode.set("doc_as_upsert", objectMapper.convertValue(true, JsonNode.class));
			final Update update = new Update.Builder(docNode.toString()).index(esIndexName).type(_type).id(_id).build();
			JestResult result = jestClient.execute(update);

			System.out.println("Response  : " + result.getJsonString());

			System.out.println("Data Restored Successfully : type :" + _type + " id : " + _id);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public List<String> getAllEsTypes(final String esIndexName) throws JsonProcessingException, IOException {

		final List<String> esTypes = new ArrayList<>();
		final String esSoruceUrl = appProperties.getEsSourceEndpoint() + esIndexName + "/_mapping/";
		// HttpHeaders
		HttpHeaders headers = new HttpHeaders();
		if ((appProperties.getEsSoruceUser() != null && !appProperties.getEsSoruceUser().isEmpty())
				&& (appProperties.getEsSorucePassword() != null && !appProperties.getEsSorucePassword().isEmpty())) {

			String auth = appProperties.getEsSoruceUser() + ":" + appProperties.getEsSorucePassword();
			byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
			String authHeader = "Basic " + new String(encodedAuth);
			headers.set("Authorization", authHeader);
		}
		//
		headers.setAccept(Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON }));
		// Request to return JSON format
		headers.setContentType(MediaType.APPLICATION_JSON);

		// HttpEntity<String>: To get result as String.
		HttpEntity<String> entity = new HttpEntity<String>(headers);

		// RestTemplate
		RestTemplate restTemplate = new RestTemplate();

		// Send request with GET method, and Headers.
		ResponseEntity<String> response = restTemplate.exchange(esSoruceUrl, HttpMethod.GET, entity, String.class);

		String result = response.getBody();

		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode actualObj = objectMapper.readTree(result);

		ObjectNode object = (ObjectNode) actualObj;

		if (object.has(esIndexName)) {

			JsonNode indexNameNode = object.get(esIndexName);

			if (indexNameNode.has("mappings")) {
				JsonNode mappingsNode = indexNameNode.get("mappings");

				Iterator<Entry<String, JsonNode>> it = mappingsNode.fields();
				while (it.hasNext()) {
					Entry<String, JsonNode> entry = it.next();
					String name = entry.getKey();

					esTypes.add(name);
				}

			}

		}

		return esTypes;
	}

}
