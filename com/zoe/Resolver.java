package com.zoe;

import org.json.*;

public interface Resolver {
	public String name();
	public JSONObject resolve(Intent intent, JSONObject full) throws IntentErrorException;
	public JSONObject getErrorObject();
	public String getErrorMessage();
}
