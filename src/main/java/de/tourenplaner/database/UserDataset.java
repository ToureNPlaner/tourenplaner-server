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

package de.tourenplaner.database;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Date;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class UserDataset {

    /*
      id                INT           NOT NULL AUTO_INCREMENT,
      Email             VARCHAR(255)  NOT NULL UNIQUE,
      Passwordhash      TEXT          NOT NULL,
      Salt              TEXT          NOT NULL,
      AdminFlag         BOOL          NOT NULL DEFAULT 0,
      Status            ENUM ('needs_verification','verified','deleted')
                                      NOT NULL DEFAULT 'needs_verification',
      FirstName         TEXT          NOT NULL,
      LastName          TEXT          NOT NULL,
      Address           TEXT          NOT NULL,
      RegistrationDate  DATETIME      NOT NULL,
      VerifiedDate      DATETIME               DEFAULT NULL,
      PRIMARY KEY (ID)
	*/

	public int userid;
	public String email;
	public String firstName;
	public String lastName;
	public String address;
	public boolean admin;
	public UserStatusEnum status;

	@JsonIgnore
	public String passwordhash;
	@JsonIgnore
	public String salt;

	public Date registrationDate;
	public Date verifiedDate;

	public UserDataset(int id, String email, String passwordhash, String salt,
			boolean isAdmin, UserStatusEnum status, String firstName,
			String lastName, String address, Date registrationDate,
			Date verifiedDate) {
		this.userid = id;
		this.email = email;
		this.passwordhash = passwordhash;
		this.salt = salt;
		this.admin = isAdmin;
		this.status = status;
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.registrationDate = registrationDate;
		this.verifiedDate = verifiedDate;
	}
}
