package eu.modaclouds.sla.notification;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.IfProfileValue;

import eu.atos.sla.datamodel.IAgreement;
import eu.atos.sla.datamodel.IGuaranteeTerm;
import eu.atos.sla.datamodel.IViolation;
import eu.atos.sla.datamodel.bean.GuaranteeTerm;
import eu.atos.sla.datamodel.bean.Penalty;
import eu.atos.sla.datamodel.bean.PenaltyDefinition;
import eu.atos.sla.datamodel.bean.Violation;
import eu.atos.sla.evaluation.guarantee.GuaranteeTermEvaluator.GuaranteeTermEvaluationResult;
import eu.modaclouds.sla.notification.RestNotifier.RestNotifierException;

/**
 * Test to show RestNotifierTest.
 * 
 * Assume there is a system property: 
 * <li>ACCOUNTING_URL : Endpoint url of the component receiving the notifications url (e.g.: http://localhost:8081)
 *   The url must expose a POST method. 
 * 
 * To run:
 *  mvn test -Dtest-profile=IntegrationTest -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
 *  
 * @author rsosa
 */
@IfProfileValue(name="test-profile", value="IntegrationTest")
public class RestNotifierTest {
	private static final String ACCOUNTING_URL = "ACCOUNTING_URL";
	
	private RestNotifier notifier;
	
	@Before
	public void setUp() throws Exception {
		String url = System.getenv(ACCOUNTING_URL);
		
		notifier = new RestNotifier(url, "application/xml");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testOnFinishEvaluation() {
		IAgreement agreement = new eu.atos.sla.datamodel.bean.Agreement();
		agreement.setAgreementId("agreement-id");
		IGuaranteeTerm term = new GuaranteeTerm();
		term.setKpiName("kpi");
		term.setName("TERM");
		GuaranteeTermEvaluationResult res = new GuaranteeTermEvaluationResult(
				Collections.<IViolation>emptyList(), 
				Arrays.asList(
						new Penalty(agreement.getAgreementId(), 
									new Date(),
									term.getKpiName(),
									new PenaltyDefinition(1, new Date(60000), "discount", "euro", "100", "P1M"),
									new Violation(agreement, term, null, "", "", new Date())
						)
				)
		);
		Map<IGuaranteeTerm, GuaranteeTermEvaluationResult> map = 
				new HashMap<IGuaranteeTerm, GuaranteeTermEvaluationResult>();
		
		map.put(term, res);
		
		try {
			notifier.onFinishEvaluation(agreement, map);
			assertTrue(true);
		} catch (RestNotifierException e) {
			fail("Failed with status " + e.getStatus());
		}
	}

}