package com.kireygroup.camel.aggregator.route;

import org.apache.camel.builder.AggregationStrategies;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.camel.util.concurrent.SynchronousExecutorService;
import org.springframework.stereotype.Component;

import com.kireygroup.camel.aggregator.model.CsvData;

import lombok.extern.slf4j.Slf4j;

import static org.apache.camel.Exchange.SPLIT_COMPLETE;
import static org.apache.camel.Exchange.SPLIT_INDEX;

@Component
@Slf4j
public class IntegrationRoute extends EndpointRouteBuilder {
	
	private static final String CSV_NEW_LINE = "\n";
	private static final int BULK_SIZE = 5;
	
	@Override
	public void configure() throws Exception {
		
		onCompletion().onCompleteOnly()
			.log("SUCCESS");
		
		onCompletion().onFailureOnly()
			.log("FAILED");
		
		from(file("data")
				.move("done")
				.moveFailed("error"))
			.transacted()
			.log("${headers[CamelFileNameConsumed]}")
			.split(body().tokenize(CSV_NEW_LINE)).streaming().stopOnException()
			.choice()
				.when(exchangeProperty(SPLIT_INDEX).isGreaterThan(0))
					.unmarshal().bindy(BindyType.Csv, CsvData.class)
					.aggregate(constant(true), AggregationStrategies.groupedBody())
						.eagerCheckCompletion()
						.executorService(new SynchronousExecutorService())
						.completionSize(BULK_SIZE)
						.completionPredicate(exchangeProperty(SPLIT_COMPLETE))
						.to(direct("persistData"));
		
		from(direct("persistData"))
			.log(">> aggregated data: ${body}")
			.to(jpa("java.util.List")
					.usePersist(true)
					.flushOnSend(true));
	}
}
