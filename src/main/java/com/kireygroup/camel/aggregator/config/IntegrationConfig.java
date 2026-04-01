package com.kireygroup.camel.aggregator.config;

import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kireygroup.camel.aggregator.model.CsvData;

@Configuration
public class IntegrationConfig {
	
	@Bean
	DataFormat dataFormat() {
		return new BindyCsvDataFormat(CsvData.class);
	}
}
