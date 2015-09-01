package com.redhat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

public class DroolsTest {

	private StatelessDecisionService service = BrmsHelper.newStatelessDecisionServiceBuilder().auditLogName("audit").build();

	@Test
	public void helloWorldTest() {
		// given
		Collection<Object> facts = new ArrayList<Object>();
		Business business = new Business();
		business.setName("test");
		facts.add(business);

		// when
		RuleResponse response = service.runRules(facts, "VerifySupplier", RuleResponse.class);

		// then
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBusiness());
		Assert.assertEquals("test", response.getBusiness().getName());
	}

	@Test
	public void shouldFilterOutAllRequestsFromKansas(){
		// scenario: business from Kansas are handled by another system - filter them out
		// given a business from Kansas
		Collection<Object> facts = new ArrayList<Object>();
		Business business = new Business();
		business.setName("bidness");
		business.setStateCode("KS");
		facts.add(business);

		// when I apply the filtering rules
		RuleResponse response = service.runRules(facts, "VerifySupplier", RuleResponse.class);

		// then the business should be filtered
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBusiness());
		Assert.assertEquals(false, response.getSuccessResult());
		// and the reason message should be "business filtered from Kansas"
		Collection<Reason> responseReasons = response.getReasons();
		Assert.assertFalse(responseReasons.isEmpty());
		Iterator<Reason> resReasons = responseReasons.iterator();
		Assert.assertEquals("business filtered from Kansas", resReasons.next().getReasonMessage());
	}

	@Test
	public void shouldProcessAllBusinessesNotFromKansas(){
		// scenario: we are responsible for all businesses not from Kansas
		// given a business from New York
		Collection<Object> facts = new ArrayList<Object>();
		Business business = new Business();
		business.setName("bidness");
		business.setStateCode("NY");
		business.setAddressLine2("Apt. 1234");
		business.setPhoneNumber("924-555-8462");
		business.setCity("Buffalo");
		business.setFederalTaxId("5");
		business.setZipCode("21045");
		business.setAddressLine1("4321 Awesome Way");
		facts.add(business);

		// when I apply the filtering rules
		RuleResponse response = service.runRules(facts, "VerifySupplier", RuleResponse.class);

		// then the business should be not be filtered
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBusiness());
		Assert.assertEquals(true, response.getSuccessResult());
		// and the validation rules should be applied to the business

	}

	@Test
	public void shouldCreateValidationErrorsForAnyFieldThatAreEmptyOrNull(){
		// scenario: all fields must have values.
		// given a business
		// and the business' zipcode is empty
		// and the business' address line 1 is null
		Collection<Object> facts = new ArrayList<Object>();
		Business business = new Business();
		business.setName("bidness");
		business.setStateCode("NY");
		business.setAddressLine2("Apt. 1234");
		business.setPhoneNumber("924-555-8462");
		business.setCity("Buffalo");
		business.setFederalTaxId("5");
		facts.add(business);
		// when I apply the validation rules
		RuleResponse response = service.runRules(facts, "VerifySupplier", RuleResponse.class);
		// then the business should be return a validation error
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBusiness());
		Assert.assertEquals(false, response.getSuccessResult());
		// and a message should say the zipcode is empty
		Collection<Reason> responseReasons = response.getReasons();
		Assert.assertFalse(responseReasons.isEmpty());
		Iterator<Reason> resReasons = responseReasons.iterator();
		Assert.assertEquals("The zipcode was empty", resReasons.next().getReasonMessage());
		// and a message should say the address is null
		Assert.assertEquals("Address line 1 was empty", resReasons.next().getReasonMessage());
	}

	@Test
	public void shouldEnrichTheTaxIdWithZipCode(){
		// scenario: we need to enrich the taxId with the zipcode for system XYZ
		// given a business
		Collection<Object> facts = new ArrayList<Object>();
		Business business = new Business();
		business.setName("bidness");
		business.setStateCode("NY");
		business.setAddressLine1("4321 Awesome Way");
		business.setAddressLine2("Apt. 1234");
		business.setPhoneNumber("924-555-8462");
		business.setCity("Buffalo");
		business.setFederalTaxId("98765");
		business.setZipCode("10002");
		facts.add(business);
		// and the business' zipcode is 10002
		// and the business' taxId is 98765

		// when I apply the enrichment rules
		RuleResponse response = service.runRules(facts, "VerifySupplier", RuleResponse.class);

		// then the business' taxId should be enriched to 98765-10002
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBusiness());
		Assert.assertEquals(true, response.getSuccessResult());
		Assert.assertEquals("98765-10002", response.getBusiness().getFederalTaxId());
	}

}
