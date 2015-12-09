/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.tourenplaner.computecore;

import de.tourenplaner.computeserver.Responder;

/**
 * This class is used to represent a request for computation
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class ComputeRequest {

	private final Responder responder;
    private final RequestData requestData;
    private FormattedStreamWriter resultObject;
	/**
	 * Constructs a new ComputeRequest using the given Responder
	 *
     * @param responder The to this compute request corresponding Responder
     */
	public ComputeRequest(Responder responder, RequestData requestData) {
		this.responder = responder;
        this.requestData = requestData;
	}

    /**
     * Gets the RequestData object holding the data of this compute Request
     */
    public RequestData getRequestData(){
        return requestData;
    }

	/**
	 * Gets the responder object which is used to send the result to the correct
	 * client connection
	 * 
	 * @return Returns the Responder object
	 */
	public Responder getResponder() {
		return responder;
	}


    /**
     * Sets the object storing the result of this computation
     *
     * @param ro the object which will hold the result
     */
    public void setResultObject(FormattedStreamWriter ro){
        this.resultObject = ro;
    }

    /**
     * Gets the StreamJsonWriter object that can write the result of this
     * ComputeRequest
     * @return the StreamJsonWriter that will write the result
     */
    public FormattedStreamWriter getResultObject() {
        return this.resultObject;
    }

}
