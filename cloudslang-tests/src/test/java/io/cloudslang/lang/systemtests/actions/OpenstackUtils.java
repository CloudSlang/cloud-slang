/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.lang.systemtests.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.oo.sdk.content.annotations.Action;
import com.hp.oo.sdk.content.annotations.Output;
import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.annotations.Response;
import com.hp.oo.sdk.content.plugin.ActionMetadata.MatchType;
import com.hp.oo.sdk.content.plugin.ActionMetadata.ResponseType;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;


public class OpenstackUtils {
	private static final String RETURN_RESULT_KEY = "returnResult";
	private static final String ID_KEY = "id";
	private static final String TOKEN_KEY = "token";
	private static final String TENANT_KEY = "tenant";
	private static final String PARSED_TOKEN_KEY = "parsedToken";
	private static final String PARSED_TENANT_KEY = "parsedTenant";
	private static final String ACCESS_KEY = "access";
	public static final String RETURN_CODE = "returnCode";
	public static final String SUCCESS_CODE = "0";
	public static final String FAILED_CODE = "-1";

	public static final String JSON_AUTHENTICATION_RESPONSE_KEY= "jsonAuthenticationResponse";

	/**
	 * Parses authentication response to get the Tenant and Token and puts them
	 * back in the executionContext.
	 *
	 *
	 */
	@SuppressWarnings("unused")
    @Action(name = "Parse Authentication",
            outputs = {
                    @Output(PARSED_TENANT_KEY),
                    @Output(PARSED_TOKEN_KEY),
                    @Output("returnCode"),
                    @Output("returnResult")
            },
            responses = {
                    @Response(text = "success", field = "returnCode", value = "0", matchType = MatchType.COMPARE_EQUAL, responseType = ResponseType.RESOLVED),
                    @Response(text = "failure", field = "returnCode", value = "-1", matchType = MatchType.COMPARE_EQUAL, responseType = ResponseType.ERROR)
            }
    )
	public Map<String, String> parseAuthentication(@Param(JSON_AUTHENTICATION_RESPONSE_KEY)String jsonAuthenticationResponse) {

        Map<String, String> results = new HashMap<>();
        try {
            JsonElement parsedResult = new JsonParser().parse(jsonAuthenticationResponse);
            JsonObject parsedObject = parsedResult.getAsJsonObject();
            JsonObject accessObject = parsedObject.getAsJsonObject(ACCESS_KEY);
            JsonObject tokenObject = accessObject.getAsJsonObject(TOKEN_KEY);

            String resultToken = tokenObject.get(ID_KEY).toString();
            resultToken = resultToken.substring(1, resultToken.length() - 1);

            JsonObject tenantObject = tokenObject.getAsJsonObject(TENANT_KEY);
            String resultTenant = tenantObject.get(ID_KEY).toString();
            resultTenant = resultTenant.substring(1, resultTenant.length() - 1);

            results.put(PARSED_TENANT_KEY, resultTenant);
            results.put(PARSED_TOKEN_KEY, resultToken);
            results.put(RETURN_RESULT_KEY, "Parsing successful.");
            if (!(StringUtils.isEmpty(resultToken) && StringUtils.isEmpty(resultTenant))) {
                results.put(RETURN_CODE, SUCCESS_CODE);
            } else {
                results.put(RETURN_CODE, FAILED_CODE);
            }
        } catch(Exception ignored){}
        return results;
	}

}
