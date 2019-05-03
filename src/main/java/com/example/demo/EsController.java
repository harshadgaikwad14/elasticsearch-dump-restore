package com.example.demo;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
public class EsController {

	@Autowired
	private EsService esService;

	@RequestMapping("/updateMapping/{indexName}/")
	public String updateMapping(@PathVariable("indexName") String esIndexName)
			throws JsonProcessingException, IOException {

		/*
		 * http://localhost:8889/updateMapping/quest.liqi/
		 */

		System.out.println("getSoruceEsMappings : indexName : " + esIndexName);
		if (esIndexName != null && !esIndexName.isEmpty()) {

			final JsonNode jsonNode = esService.getSoruceEsMapping(esIndexName);
			// final String destIndexName= esIndexName+System.currentTimeMillis();
			esService.createEsMapping(esIndexName, jsonNode);

			return "ElasticSearch mapping Updated Successfully For New Idex : " + esIndexName;
		}

		return "Please Provide Idex Name";
	}

	@RequestMapping("/updateData/{indexName}/{esType}")
	public String updateData(@PathVariable("indexName") String esIndexName, @PathVariable("esType") String esType)
			throws JsonProcessingException, IOException, InterruptedException {

		/*
		 * http://localhost:8889/updateData/quest.liqi/loans
		 */

		System.out.println("getSoruceEsMappings : indexName : " + esIndexName);
		if (esIndexName != null && !esIndexName.isEmpty()) {

			if (esType.equalsIgnoreCase("all")) {
				final List<String> getAllEsTypes = esService.getAllEsTypes(esIndexName);
				for (String type : getAllEsTypes) {
					System.out.println("==================> Start Data Updation esIndexName : " + esIndexName
							+ " esType : " + type);
					final JsonNode jsonNode = esService.getEsTypeData(esIndexName, type);
					if (jsonNode != null) {

						esService.updateEsData(esIndexName, jsonNode);
					}
					System.out.println("==================>Finished Data Updation esIndexName : " + esIndexName
							+ " esType : " + type);
				}
			} else {

				final JsonNode jsonNode = esService.getEsTypeData(esIndexName, esType);
				esService.updateEsData(esIndexName, jsonNode);
			}

			return "Data Updated Successfully For Idex : " + esIndexName;
		}

		return "Please Provide Idex Name";
	}

	@RequestMapping("/updateData/{indexName}/{esType}/{_id}")
	public String updateData(@PathVariable("indexName") String esIndexName, @PathVariable("esType") String esType,
			@PathVariable("_id") String _id) throws JsonProcessingException, IOException, InterruptedException {

		/*
		 * http://localhost:8889/updateData/quest.liqi/loans/1
		 */

		System.out.println("getSoruceEsMappings : indexName : " + esIndexName);
		if (esIndexName != null && !esIndexName.isEmpty()) {

			final JsonNode jsonNode = esService.getEsTypeSpecificData(esIndexName, esType, _id);
			esService.updateEsDataSpecific(esIndexName, jsonNode);
			return "Data Updated Successfully For Idex : " + esIndexName + " Type : " + esType + " Id : " + _id;
		}

		return "Please Provide Idex Name";
	}

}
