package no.nav.dokdistdpo.consumer.saf.graphql;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Map;

@Builder
public record SafGraphQLRequest(
		String query,
		String operationName,
		Map<String, Object> variables) {

	@JsonCreator
	public SafGraphQLRequest(@JsonProperty("query") String query,
							 @JsonProperty("operationName") String operationName,
							 @JsonProperty("variables") Map<String, Object> variables) {
		this.query = query;
		this.operationName = operationName;
		this.variables = variables;
	}
}
