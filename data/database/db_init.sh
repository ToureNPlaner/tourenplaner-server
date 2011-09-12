#! /bin/bash

## if errors occur, next command within script will be executed
## (because of option --force)
## because of that the script will continue without error if database exists
## to see all errors you should read the output carefully

mysql -u root -p --verbose --force < db_init_script

## use following commands in your mysql client to create a new user with
## rights on your previously created tables
#CREATE USER tnpuser IDENTIFIED BY 'yourpassword'; 
#grant usage on *.* to tnpuser@localhost identified by 'yourpassword'; 
#grant all privileges on tourenplaner.* to tnpuser@localhost;
