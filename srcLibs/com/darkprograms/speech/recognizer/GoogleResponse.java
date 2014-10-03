package com.darkprograms.speech.recognizer;

import java.util.ArrayList;

/******************************************************************************
 * Class that holds the response and confidence of a Google recognizer request
 *
 * @author Luke Kuza, Duncan Jauncey, Aaron Gokaslan
 ******************************************************************************/
public class GoogleResponse {

    /**
     * Variable that holds the response
     */
    private String response;
    /**
     * Variable that holds the confidence score
     */
    private String confidence;

    /**
     * ArrayList that holds other possible responses for this request.
     */
    private ArrayList<String> otherPossibleResponses = new ArrayList<String>(20);

    /**
     * Constructor
     */
    public GoogleResponse() {

    }


    /**
     * Gets the response text of what was said in the submitted Audio to Google
     *
     * @return String representation of what was said
     */
    public String getResponse() {
        return response;
    }

    /**
     * Set the response
     *
     * @param response The response
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * Gets the confidence score for the specific request
     *
     * @return The confidence score, ex .922343324323
     */
    public String getConfidence() {
        return confidence;
    }

    /**
     * Set the confidence score for this request
     *
     * @param confidence The confidence score
     */
    protected void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    /**
     * Get other possible responses for this request.
     * @return other possible responses
     */
    public ArrayList<String> getOtherPossibleResponses() {
        return otherPossibleResponses;
    }
    
    /**
     * Gets all returned responses for this request
     * @return All returned responses
     */
    public ArrayList<String> getAllPossibleResponses() {
    	ArrayList<String> tmp = otherPossibleResponses;
    	tmp.add(0,response);
    	return tmp;
    }

}
