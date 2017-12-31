package com.zoe.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;
import org.junit.*;

import com.zoe.*;

public class TestAgent {
	private Agent testAgent;
	
	@Before
	public void startAgent() throws IOException, TimeoutException{
		testAgent = new Agent("test", false);
	}
	
	/**
	 * When the appropriate intent to be resolved can be resolved, the agent's <code>{@link Agent#intentResolver(JSONObject)}</code> method returns the correct resolution of the intent.
	 * @throws NotAnIntentException
	 * @throws NoResolverException
	 * @throws ErrorMessageException 
	 */
	@Test
	public void testInner() throws NotAnIntentException, NoResolverException, ErrorMessageException{
		testAgent.addResolver(new Resolver("a", "ack"){

			@Override
			public JSONObject resolve(Intent intent, JSONObject full) throws IntentErrorException {
				JSONObject dummy = new JSONObject();
				return dummy;
			}

			@Override
			public JSONObject getErrorObject(IntentErrorException ex) {
				return null;
			}
		});
		
		/*
		 * fullIn:
		 * {
		 * 	  'intent': 'b'
		 * 	  'args':{
		 * 		    'intent': 'a'
		 * 	  }
		 * }
		 */
		JSONObject fullIn = new JSONObject();
		fullIn.put("intent", "b");
		JSONObject innerIn = new JSONObject();
		innerIn.put("intent", "a");
		fullIn.put("args", innerIn);
		String input = testAgent.intentResolver(fullIn).toString();
		
		/*
		 * fullOut:
		 * {
		 * 	  'intent': 'b'
		 * 	  'args':{
		 * 	      'data': 'ack'
		 * 	  }
		 * }
		 */
		JSONObject fullOut = new JSONObject();
		fullOut.put("intent", "b");
		JSONObject innerOut = new JSONObject();
		innerOut.put("data", "ack");
		fullOut.put("args", innerOut);
		String expected = fullOut.toString();
		
		assertEquals(expected, input);
	}
	
	/**
	 * When the appropriate intent to be resolved cannot be resolved, the agent skips the message. This is done by throwing a <code>{@link NoResolverException}</code>, which is then caught by the consumer, that ignores the input.
	 * @throws NotAnIntentException
	 * @throws NoResolverException
	 * @throws ErrorMessageException 
	 */
	@Test(expected = NoResolverException.class)
	public void testOuter() throws NotAnIntentException, NoResolverException, ErrorMessageException{
		testAgent.addResolver(new Resolver("b", "ack"){

			@Override
			public JSONObject resolve(Intent intent, JSONObject full) throws IntentErrorException {
				JSONObject dummy = new JSONObject();
				return dummy;
			}

			@Override
			public JSONObject getErrorObject(IntentErrorException ex) {
				return null;
			}			
		});
		
		/*
		 * fullIn:
		 * {
		 * 	  'intent': 'b'
		 * 	  'args':{
		 * 		    'intent': 'a'
		 * 	  }
		 * }
		 */
		JSONObject fullIn = new JSONObject();
		fullIn.put("intent", "b");
		JSONObject innerIn = new JSONObject();
		innerIn.put("intent", "a");
		fullIn.put("args", innerIn);
		testAgent.intentResolver(fullIn).toString();
	}
	
	/**
	 * If trying to resolve an intent with a <code>{@link Resolver}</code> a <code>{@link IntentErrorException}</code> exception is thrown, the message will be turned into an error.
	 * @throws NotAnIntentException
	 * @throws NoResolverException
	 * @throws ErrorMessageException
	 */
	@Test
	public void testErrorOut() throws NotAnIntentException, NoResolverException, ErrorMessageException{
		testAgent.addResolver(new Resolver("a", "ack"){
			@Override
			public JSONObject resolve(Intent intent, JSONObject full) throws IntentErrorException {
				throw new IntentErrorException("This is the error message.");
			}

			@Override
			public JSONObject getErrorObject(IntentErrorException ex) {
				JSONObject dummy = new JSONObject();
				dummy.put("dummy", "blah");
				return dummy;
			}			
		});
		

		/*
		 * fullIn:
		 * {
		 * 	  'intent': 'b'
		 * 	  'args':{
		 * 		    'intent': 'a'
		 * 	  }
		 * }
		 */
		
		JSONObject fullIn = new JSONObject();
		fullIn.put("intent", "b");
		JSONObject innerIn = new JSONObject();
		innerIn.put("intent", "a");
		fullIn.put("args", innerIn);
		String input = testAgent.intentResolver(fullIn).toString();
		
		/*
		 * fullOut
		 * {
		 *     'error':{
		 *         'dummy': 'blah'
		 *     }
		 *     'intent': 'b'
		 *     'args':{
		 *         'error':{
		 *             'dummy': 'blah'
		 *         }
		 *     }
		 * }
		 */
		JSONObject fullOut = new JSONObject();		
		JSONObject error = new JSONObject();
		error.put("dummy", "blah");		
		fullOut.put("error", error);
		fullOut.put("intent", "b");		
		JSONObject args = new JSONObject();
		args.put("error", error);
		fullOut.put("args", args);
		String expected = fullOut.toString();
		
		assertEquals(expected, input);
	}
	/**
	 * If any of the keys in any of the objects of the message is "error", the resolver will throw a <code>{@link ErrorMessageException}</code> exception
	 * @throws NoResolverException
	 * @throws NotAnIntentException
	 * @throws ErrorMessageException
	 */
	@Test(expected = ErrorMessageException.class)
	public void testErrorIn() throws NoResolverException, NotAnIntentException, ErrorMessageException{
		testAgent.addResolver(new Resolver("b", "ack"){

			@Override
			public JSONObject resolve(Intent intent, JSONObject full) throws IntentErrorException {
				return new JSONObject();
			}

			@Override
			public JSONObject getErrorObject(IntentErrorException ex) {
				return new JSONObject();
			}
			
		});	
		/*
		 * fullIn
		 * {
		 *     'error':{
		 *         'dummy': 'blah'
		 *     }
		 *     'intent': 'b'
		 *     'args':{
		 *         'error':{
		 *             'dummy': 'blah'
		 *         }
		 *     }
		 * }
		 */
		JSONObject fullIn = new JSONObject();		
		JSONObject error = new JSONObject();
		error.put("dummy", "blah");		
		fullIn.put("error", error);
		fullIn.put("intent", "b");		
		JSONObject args = new JSONObject();
		args.put("error", error);
		fullIn.put("args", args);
		testAgent.intentResolver(fullIn);
	}
	/**
	 * In this test, the intent that the resolver can resolve is quoted, and thus it ignores the message
	 * @throws NoResolverException
	 * @throws NotAnIntentException
	 * @throws ErrorMessageException
	 */
	@Test(expected = NoResolverException.class)
	public void testQuotations() throws NoResolverException, NotAnIntentException, ErrorMessageException{
		testAgent.addResolver(new Resolver("a", "ack"){

			@Override
			public JSONObject resolve(Intent intent, JSONObject full) throws IntentErrorException {
				JSONObject dummy = new JSONObject();
				return dummy;
			}

			@Override
			public JSONObject getErrorObject(IntentErrorException ex) {
				return null;
			}
		});
		
		/*
		 * fullIn:
		 * {
		 * 	  'intent': 'b'
		 * 	  'args!':{
		 * 		    'intent': 'a'
		 * 	  }
		 * }
		 */
		JSONObject fullIn = new JSONObject();
		fullIn.put("intent", "b");
		JSONObject innerIn = new JSONObject();
		innerIn.put("intent", "a");
		fullIn.put("args!", innerIn);
		testAgent.intentResolver(fullIn).toString();		
	}
}
