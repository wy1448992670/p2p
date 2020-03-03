package controllers;

import annotation.Check;
import play.Play;
import play.mvc.Before;

public class TrustFunds extends BaseController{
     
	@Before
	static void checkAccess() {
		Check check = getActionAnnotation(Check.class);
		
		if(check != null) {
			String trustFunds = Play.configuration.getProperty("pay.trustFunds","false");
	        String[] request = check.value();
	        String action = request[0];
	        
	        if(trustFunds.equals(action)) {
		        
	          renderTemplate("Application/trustfunds.html");
	        }
	    }
	}
	
}
