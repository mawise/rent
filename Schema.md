## Users

 * uuid (pk)
 * email
 * token
 * password_salt
 * password_hash

 ```mysql
 CREATE TABLE `rentshape`.`users` (
`uuid` VARCHAR(45) NOT NULL,
`email` VARCHAR(255) NOT NULL,
`token` VARCHAR(255) NOT NULL,
`password_salt` VARBINARY(45) NOT NULL,
`password_hash` VARBINARY(45) NOT NULL,
PRIMARY KEY (`uuid`),
UNIQUE (`email`));
```

## Application

 * Last Name
 * First Name
 * Middle Name
 * SSN or ITIN
 * Other Names
 * Work Phone
 * Home Phone
 * Cell Phone
 * DOB
 * Email
 * Photo ID Type
 * ID Number
 * ID Issuer
 * ID Expiration
 * Proposed Occupants
 * Pets?
 * Describe Pets
 * Waterbed?
 * Describe Waterbed
 * Source (How did you hear about this rental)
 * Bankrupcy?
 * Evicted?
 * Drug conviction?
 
  ```mysql
CREATE TABLE `rentshape`.`applications` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `last_name` VARCHAR(255) NULL,
  `first_name` VARCHAR(255) NULL,
  `middle_name` VARCHAR(255) NULL,
  `ssn` VARCHAR(255) NULL,
  `other_names` VARCHAR(255) NULL,
  `home_phone` VARCHAR(255) NULL,
  `work_phone` VARCHAR(255) NULL,
  `cell_phone` VARCHAR(255) NULL,
  `dob` VARCHAR(255) NULL,
  `email` VARCHAR(255) NULL,
  `id_type` VARCHAR(255) NULL,
  `id_number` VARCHAR(255) NULL,
  `id_issuer` VARCHAR(255) NULL,
  `id_experation` VARCHAR(255) NULL,
  `user_uuid` VARCHAR(45) NULL,
  `occupants` VARCHAR(255) NULL,
  `pets` VARCHAR(255) NULL,
  `waterbed` VARCHAR(255) NULL,
  `source` VARCHAR(255) NULL,
  `bankruptcy` VARCHAR(255) NULL,
  `evicted` VARCHAR(255) NULL,
  `drug_conviction` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  INDEX `user_uuid_idx` (`user_uuid` ASC),
  CONSTRAINT `user_uuid`
    FOREIGN KEY (`user_uuid`)
    REFERENCES `rentshape`.`users` (`uuid`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
```


 
 * Addresses
	* Street
	* City
	* State
	* Zip
	* Date In
	* Date Out
	* Landlord Name
	* Landlord phone number
	* Reason for moving out
	* Rent

```mysql
CREATE TABLE `rentshape`.`addresses` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `address` VARCHAR(255) NULL,
  `dates` VARCHAR(255) NULL,
  `landlord_name` VARCHAR(255) NULL,
  `landlord_number` VARCHAR(255) NULL,
  `end_reason` VARCHAR(255) NULL,
  `rent` VARCHAR(255) NULL,
  `application_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `application_id_idx` (`application_id` ASC),
  CONSTRAINT `application_id`
    FOREIGN KEY (`application_id`)
    REFERENCES `rentshape`.`applications` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
```

 * Employers
 	* Employer Name
 	* Job Title
 	* Dates of Employment
 	* Address
 	* Phone number
 	* City
 	* State
 	* Zip
 	* Name of Supervisor/HR Manager
 	* Income amount
 	* Income frequency

```mysql
CREATE TABLE `rentshape`.`employers` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NULL,
  `title` VARCHAR(255) NULL,
  `dates` VARCHAR(255) NULL,
  `address` VARCHAR(255) NULL,
  `phone` VARCHAR(255) NULL,
  `supervisor` VARCHAR(255) NULL,
  `income_amount` VARCHAR(255) NULL,
  `income_frequency` VARCHAR(255) NULL,
  `application_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `application_id_idx` (`application_id` ASC),
  CONSTRAINT `application_id`
    FOREIGN KEY (`application_id`)
    REFERENCES `rentshape`.`applications` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
```
 	
 * Other Income
 	* Source
 	* Amount
 	* Frequency

```mysql
CREATE TABLE `rentshape`.`incomes` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `source` VARCHAR(255) NULL,
  `amount` VARCHAR(255) NULL,
  `frequency` VARCHAR(255) NULL,
  `application_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `application_id_idx` (`application_id` ASC),
  CONSTRAINT `application_id`
    FOREIGN KEY (`application_id`)
    REFERENCES `rentshape`.`applications` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
```
 	
 	
 * Bank
 	* Name
 	* Address
 	* Account Number

```mysql
CREATE TABLE `rentshape`.`banks` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  `address` VARCHAR(95) NULL,
  `account_number` VARCHAR(45) NULL,
  `application_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `application_id_idx` (`application_id` ASC),
  CONSTRAINT `application_id`
    FOREIGN KEY (`application_id`)
    REFERENCES `rentshape`.`applications` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
```

 * Debt
   * Creditor
   * Address
   * Phone Number
   * Monlty Payment

 * Emergency
   * Name
   * Address
   * Relationship
   * Phone

 * Reference
   * Name
   * Address
   * Length
   * Occupation
   * Phone

 * Car
   * Make
   * Model
   * Year
   * License