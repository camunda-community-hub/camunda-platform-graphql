package org.camunda.bpm.extension.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.extension.graphql.infratest.bpm.BaseTest;
import org.camunda.bpm.extension.graphql.infratest.bpm.services.WeatherCheckService;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;

import static org.camunda.bpm.extension.graphql.infratest.comparators.Comparators.comparators;
import static org.camunda.bpm.extension.graphql.infratest.comparators.DateExpressionMatcher.isDate;
import static org.camunda.bpm.extension.graphql.infratest.comparators.IsANumberExpressionMatcher.isNumeric;
import static org.camunda.bpm.extension.graphql.infratest.scenarios.BDD.*;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

public class HistoricProcessInstanceTest extends BaseTest {

    private static final String PROCESS_KEY = "weather-process";
    private static final String BUSINESS_KEY = "552365";

    @Autowired
    private WeatherCheckService service;
    private ProcessInstance processInstance;

    private final CustomComparator comparators = comparators(STRICT,
            isNumeric( "historicProcessInstances[0].id"),
            isNumeric( "historicProcessInstances[0].durationInMillis"),
            isNumeric("historicProcessInstances[0].rootProcessInstanceId"),
            isDate("historicProcessInstances[0].startTime"),
            isDate("historicProcessInstances[0].endTime")
    );

    @Override
    public void setUp() throws Exception {
        super.setUp();
        scenario.load("historic-process-instance-scenarios.json");
        when(service.checkWeather()).thenReturn(30);
        processInstance = runtimeService.startProcessInstanceByKey(PROCESS_KEY, BUSINESS_KEY);

    }

    @Test
    public void shouldReturnTheHistoricProcessInstance() throws JSONException, JsonProcessingException {
        Given("a query to search a historic process instance by businessKey");
            String graphqlQuery = "query to find process instance by businessKey";
        When("graphql is called");
            ExecutionResult executionResult = graphQL.execute(query(graphqlQuery));
            String result = new ObjectMapper().writeValueAsString(executionResult.getData());
        Then("the result should be");
            assertEquals(s("result of historic process instances"), result, comparators);
    }
}