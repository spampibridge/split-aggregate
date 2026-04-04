package com.kireygroup.camel.aggregator.route;

import org.apache.camel.builder.AggregationStrategies;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.camel.util.concurrent.SynchronousExecutorService;
import org.springframework.stereotype.Component;

import com.kireygroup.camel.aggregator.model.CsvData;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IntegrationRoute extends EndpointRouteBuilder {
	
	private static final String SPLIT_END_EXPRESSION = "${exchangeProperty.CamelSplitComplete} == true";
	private static final String SKIP_HDR_EXPRESSION = "${exchangeProperty.CamelSplitIndex} > 0";
	private static final String CSV_NEW_LINE = "\n";
	private static final int BULK_SIZE = 5;
	
	@Override
	public void configure() throws Exception {
		
		from(file("data").move("done").moveFailed("error"))
			.transacted()
			.log("${headers[CamelFileNameConsumed]}")
			.split(body().tokenize(CSV_NEW_LINE)).streaming().stopOnException()
			.choice()
				.when(simple(SKIP_HDR_EXPRESSION))
					.unmarshal().bindy(BindyType.Csv, CsvData.class)
					.aggregate(constant(true), AggregationStrategies.groupedBody())
					.eagerCheckCompletion()
					.executorService(new SynchronousExecutorService())
					.completionSize(BULK_SIZE)
					.completionPredicate(simple(SPLIT_END_EXPRESSION))
					.to(direct("persistData"));
		
		from(direct("persistData"))
			.log(">> aggregated data: ${body}")
			.to(jpa("java.util.List").usePersist(true).flushOnSend(true));
	}
}
