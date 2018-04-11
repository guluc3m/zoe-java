package org.zoe.test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zoe.*;

public class TestAgent {
	private Agent testAgent;
	
	@BeforeEach
	public void startAgent() throws IOException, TimeoutException{
		testAgent = new Agent("test");
	}
	
	/**
	 * When the appropriate intent to be resolved can be resolved, the agent's <code>{@link Agent#intentResolver(JSONObject)}</code> method returns the correct resolution of the intent.
	 * @throws NotAnIntentException
	 * @throws NoResolverException
	 * @throws ErrorMessageException 
	 */
	@Test
	public void testInner() throws NotAnIntentException, NoResolverException, ErrorMessageException{
		testAgent.addResolver(new Resolver("a"){

			@Override
			public JSONObject resolve(Intent intent, JSONObject full) throws IntentErrorException {
				JSONObject dummy = new JSONObject();
				dummy.put("data", "ack");
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
	@Test
	public void testOuter() throws NotAnIntentException, NoResolverException, ErrorMessageException{
		testAgent.addResolver(new Resolver("b"){

			@Override
			public JSONObject resolve(Intent intent, JSONObject full) throws IntentErrorException {
				JSONObject dummy = new JSONObject();
				dummy.put("data", "ack");
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
		assertThrows(NoResolverException.class, () -> testAgent.intentResolver(fullIn).toString());
	}
	
	/**
	 * If trying to resolve an intent with a <code>{@link Resolver}</code> a <code>{@link IntentErrorException}</code> exception is thrown, the message will be turned into an error.
	 * @throws NotAnIntentException
	 * @throws NoResolverException
	 * @throws ErrorMessageException
	 */
	@Test
	public void testErrorOut() throws NotAnIntentException, NoResolverException, ErrorMessageException{
		testAgent.addResolver(new Resolver("a"){
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
		 *     'error': 'This is the error message.'
		 *     'intent': 'b'
		 *     'args':{
		 *         'error': 'This is the error message.'
		 *         'dummy': 'blah'
		 *     }
		 * }
		 */
		JSONObject fullOut = new JSONObject();	
		fullOut.put("error", "This is the error message.");
		fullOut.put("intent", "b");		
		JSONObject args = new JSONObject();
		args.put("error", "This is the error message.");
		args.put("dummy", "blah");		
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
	@Test
	public void testErrorIn() throws NoResolverException, NotAnIntentException, ErrorMessageException{
		testAgent.addResolver(new Resolver("b"){

			@Override
			public JSONObject resolve(Intent intent, JSONObject full) throws IntentErrorException {
				JSONObject dummy = new JSONObject();
				dummy.put("data", "ack");
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
		assertThrows(ErrorMessageException.class, () -> testAgent.intentResolver(fullIn));
	}
	/**
	 * In this test, the intent that the resolver can resolve is quoted, and thus it ignores the message
	 * @throws NoResolverException
	 * @throws NotAnIntentException
	 * @throws ErrorMessageException
	 */
	@Test
	public void testQuotations() throws NoResolverException, NotAnIntentException, ErrorMessageException{
		testAgent.addResolver(new Resolver("a"){

			@Override
			public JSONObject resolve(Intent intent, JSONObject full) throws IntentErrorException {
				JSONObject dummy = new JSONObject();
				dummy.put("data", "ack");
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
		assertThrows(NoResolverException.class, () -> testAgent.intentResolver(fullIn));
	}
	/**
	 * If there is an array, it will try to resolve the first intent the array has
	 * @throws NoResolverException
	 * @throws NotAnIntentException
	 * @throws ErrorMessageException
	 */
	@Test
	public void testArray() throws NoResolverException, NotAnIntentException, ErrorMessageException {
		testAgent.addResolver(new Resolver("a"){

			@Override
			public JSONObject resolve(Intent intent, JSONObject full) throws IntentErrorException {
				JSONObject dummy = new JSONObject();
				dummy.put("data", "ack");
				return dummy;
			}

			@Override
			public JSONObject getErrorObject(IntentErrorException ex) {
				return new JSONObject();					
			}
		});
		
		/*
		 *	fullIn:
		 *	{
		 *		'data': 'hey',
		 *		'params': [
		 *			{
		 *				'intent', 'a'
		 *			},
		 *			{
		 *				'intent', 'b'
		 *			}
		 *		]
		 *	} 
		 */
		JSONObject a0, b0, fullIn;
		fullIn = new JSONObject();
		a0 = new JSONObject();
		b0 = new JSONObject();
		JSONArray params0 = new JSONArray();
		a0.put("intent", "a");
		b0.put("intent", "b");
		params0.put(a0);
		params0.put(b0);
		fullIn.put("data", "hey");
		fullIn.put("params", params0);
		String output = testAgent.intentResolver(fullIn).toString();
		/*
		 *	fullOut:
		 *	{
		 *		'data': 'hey',
		 *		'params': [
		 *			{
		 *				'data': 'ack'
		 *			},
		 *			{
		 *				'intent': 'b'
		 *			}
		 *		]
		 *	} 
		 */
		JSONObject a, b, fullOut;
		fullOut = new JSONObject();
		a = new JSONObject();
		b = new JSONObject();
		JSONArray params = new JSONArray();
		a.put("data", "ack");
		b.put("intent", "b");
		params.put(a);
		params.put(b);
		fullOut.put("data", "hey");
		fullOut.put("params", params);
		String expected = fullOut.toString();
		assertEquals(expected, output);
	}
	/**
	 * If there is an array, and there are no resolvers to resolve the first intent, ignore the message
	 */
	@Test
	public void testArray2() {
		testAgent.addResolver(new Resolver("b") {

			@Override
			public JSONObject resolve(Intent intent, JSONObject full) throws IntentErrorException {
				JSONObject dummy = new JSONObject();
				dummy.put("data", "ack");
				return dummy;
			}

			@Override
			public JSONObject getErrorObject(IntentErrorException ex) {
				return new JSONObject();
			}
			
			/*
			 *	fullIn:
			 *	{
			 *		'data': 'hey',
			 *		'params': [
			 *			{
			 *				'intent', 'a'
			 *			},
			 *			{
			 *				'intent', 'b'
			 *			}
			 *		]
			 *	} 
			 */
		});
		JSONObject a0, b0, fullIn;
		fullIn = new JSONObject();
		a0 = new JSONObject();
		b0 = new JSONObject();
		JSONArray params0 = new JSONArray();
		a0.put("intent", "a");
		b0.put("intent", "b");
		params0.put(a0);
		params0.put(b0);
		fullIn.put("data", "hey");
		fullIn.put("params", params0);
		assertThrows(NoResolverException.class, () -> testAgent.intentResolver(fullIn));
	}
}
