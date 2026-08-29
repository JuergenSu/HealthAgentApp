package de.fitnesscoach.agent;

import static org.junit.Assert.*;import java.io.*;import java.nio.charset.StandardCharsets;import org.junit.Test;import de.fitnesscoach.BuildConfig;

public class CoachGatewayContractTest {
 @Test public void usesProductionGatewayByDefault(){assertEquals("https://res-arkana.com/gateway/",BuildConfig.COACH_GATEWAY_URL);assertEquals("https://res-arkana.com/gateway/api/v1/coach",CoachGatewayClient.coachEndpoint(BuildConfig.COACH_GATEWAY_URL));}
 @Test public void normalizesTrailingSlashesWhenBuildingCoachEndpoint(){assertEquals("https://res-arkana.com/gateway/api/v1/coach",CoachGatewayClient.coachEndpoint("https://res-arkana.com/gateway///"));}
 @Test public void parsesSharedFinalFixture()throws Exception{CoachGatewayClient.Reply r=CoachGatewayClient.parseResponse(200,fixture("final.json"));assertEquals("FINAL",r.type);assertTrue(r.message.contains("Heute"));}
 @Test public void parsesSharedToolCallFixture()throws Exception{CoachGatewayClient.Reply r=CoachGatewayClient.parseResponse(200,fixture("tool_call.json"));assertEquals("TOOL_CALL",r.type);assertEquals("get_today_status",r.toolCall.name);}
 @Test public void rejectsSharedUnknownToolFixture()throws Exception{try{CoachGatewayClient.parseResponse(200,fixture("invalid_tool_call.json"));fail("unknown tool must fail closed");}catch(CoachGatewayClient.GatewayException e){assertEquals(502,e.status);}}
 @Test public void distinguishesValidationAndUpstreamErrors()throws Exception{try{CoachGatewayClient.parseResponse(400,"{\"error\":{\"message\":\"Ungültig\"}}");fail();}catch(CoachGatewayClient.GatewayException e){assertTrue(e.validation);}try{CoachGatewayClient.parseResponse(503,"{\"error\":{\"message\":\"Nicht verfügbar\"}}");fail();}catch(CoachGatewayClient.GatewayException e){assertFalse(e.validation);}}
 private String fixture(String name)throws IOException{try(InputStream in=getClass().getClassLoader().getResourceAsStream(name)){if(in==null)throw new FileNotFoundException(name);return new String(in.readAllBytes(),StandardCharsets.UTF_8);}}
}
