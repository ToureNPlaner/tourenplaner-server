#! /bin/bash

DATABASE_HOST="localhost"
DATABASE_PORT="3306"
DATABASE_USER="root"

if [ $# -ge 1 ]
then
  if [ $1 == "--help" -o $1 == "-h" ]
  then
    echo "
--- Database Initializing Script for Tourenplaner ---

Usage: 

$0 [-h | --help]
$0 [-H mysqlhost] [-P mysqlport] [-U mysqluser]

You need only to set options if you do not want to use the default values.

-H mysqlhost
    The default host address for the MySQL de.tourenplaner.server is \"$DATABASE_HOST\".

    Use \"localhost\" if you are running this script $0 
    on the host of the MySQL de.tourenplaner.server.

    If the MySQL de.tourenplaner.server is not at localhost you have to set this option.

-P mysqlport
    The default port is \"$DATABASE_PORT\".

-U mysqluser
    The default user is the user \"$DATABASE_USER\".

    The user \"mysqluser\" needs for each task of this script $0
    certain rights. Usually the mysql user \"root\" should have all
    needed rights.

Overview of the different tasks

  * Create de.tourenplaner.database
    
    You can create the de.tourenplaner.database, which you want to use for Tourenplaner.

    The user \"mysqluser\" must have the right to create databases.

  * Create tables

    You can create all tables which you need for Tourenplaner.

    The user \"mysqluser\" needs the right to create tables for the 
    de.tourenplaner.database which you want to use for Tourenplaner.

  * Insert admin user for Tourenplaner

    You can insert the admin user for Tourenplaner into the de.tourenplaner.database.

    The user \"mysqluser\" needs the right to execute insert statements
    for the de.tourenplaner.database which you want to use for Tourenplaner.
    
  * Create or modify mysql user

    You can create a new mysql user with all privileges for the de.tourenplaner.database
    which you want to use for Tourenplaner. If the user already exists,
    this script will grant to the user all privileges for the de.tourenplaner.database.

    The user \"mysqluser\" needs the right to grant to other users all 
    privileges for the de.tourenplaner.database which you want to use with Tourenplaner.
    The user \"mysqluser\" should also have the right to create new users.
"
    exit 1
  fi
fi

while getopts "H:P:U" opt;
do
  case $opt in
    H) DATABASE_HOST=$OPTARG ;;
    P) DATABASE_PORT=$OPTARG ;;
    U) DATABASE_USER=$OPTARG ;;
    *) echo "To display the help use the following command: $0 --help" ; exit 1 ;;
esac
done

echo "
--- Database Initializing Script for Tourenplaner ---

The MySQL de.tourenplaner.server will be connected with following values:

  MySQL de.tourenplaner.server host: $DATABASE_HOST
  MySQL de.tourenplaner.server port: $DATABASE_PORT
  MySQL de.tourenplaner.server user: $DATABASE_USER

See the help for command line options to change these values.
To view the help start the script as follows: $0 --help

If you want to see if errors occur, read the output of each task carefully.

To quit the script, press CTRL+C."

echo "

Please enter the name of the de.tourenplaner.database which Tourenplaner should use."
read -p "Database name (e.g. \"tourenplaner\"): " DATABASE_NAME

echo "
Please enter the password for the mysql user $DATABASE_USER."
read -s -p "Password: " DATABASE_PASSWORD

read -p "
Task \"Create de.tourenplaner.database\":

  Tourenplaner needs an own de.tourenplaner.database to work in private mode.

  Do you want to create the de.tourenplaner.database \"$DATABASE_NAME\"?
  Choose yes or no (y/n): " CHOSEN_OPTION
if [ ${CHOSEN_OPTION,,} == "y" -o ${CHOSEN_OPTION,,} == "yes" ]
then
  echo
  mysql -u $DATABASE_USER --password=$DATABASE_PASSWORD --verbose --force \
    --host=$DATABASE_HOST --port=$DATABASE_PORT <<< "create de.tourenplaner.database $DATABASE_NAME;"
fi

read -p "
Task \"Create tables\":

  Tourenplaner needs the tables to work in private mode.

  The de.tourenplaner.database \"$DATABASE_NAME\" must exists for this task.

  Do you want to create the tables for the de.tourenplaner.database \"$DATABASE_NAME\"?
  Choose yes or no (y/n): " CHOSEN_OPTION
if [ ${CHOSEN_OPTION,,} == "y" -o ${CHOSEN_OPTION,,} == "yes" ]
then
  echo
  # If errors occur, next command within script will be executed (because of option --force)
  # So the script will continue without error if a table already exists
  mysql -u $DATABASE_USER --password=$DATABASE_PASSWORD --verbose --force \
    --host=$DATABASE_HOST --port=$DATABASE_PORT $DATABASE_NAME < db_init_script.sql
fi

read -p "
Task \"Insert admin user for Tourenplaner\":

  Tourenplaner needs the admin user to work in private mode.

  The de.tourenplaner.database \"$DATABASE_NAME\" and the tables must exist for this task.

  Do you want to insert the admin user for Tourenplaner into the de.tourenplaner.database?
  Choose yes or no (y/n): " CHOSEN_OPTION
if [ ${CHOSEN_OPTION,,} == "y" -o ${CHOSEN_OPTION,,} == "yes" ]
then
  read -p "
  Please enter the username for the Tourenplaner admin user.
  (must be a valid email address)
  Username (e.g. \"root@tourenplaner.de\"): " TNP_USER

  echo "
  Please enter the password for the Tourenplaner admin user."
  while [ $TNP_USER_PW != $TNP_USER_PW_CONTROL ]
  do
    read -s -p "  Enter password: " TNP_USER_PW 
    read -s -p "
  Repeat password:" TNP_USER_PW_CONTROL
    echo

    if [ $TNP_USER_PW != $TNP_USER_PW_CONTROL ]
    then
      echo "
  The entered passwords are not equal, please try again."
    fi
  done

  TNP_USER_SALT=`dd if=/dev/urandom bs=4K count=1 2> /dev/null| shasum | grep -o -e '[[:alnum:]]*'`

  TNP_USER_PW_HASH=`echo -n "$TNP_USER_PW:$TNP_USER_SALT" | shasum | grep -o -e '[[:alnum:]]*'`

  mysql -u $DATABASE_USER --password=$DATABASE_PASSWORD --verbose --force \
    --host=$DATABASE_HOST --port=$DATABASE_PORT $DATABASE_NAME <<<  "
     INSERT INTO Users (id, Email, Passwordhash, Salt, AdminFlag, Status, 
       FirstName, LastName, Address, RegistrationDate, VerifiedDate) 
     VALUES('1', '$TNP_USER', '$TNP_USER_PW_HASH', '$TNP_USER_SALT', '1', 'verified', 
       'ro', 'ot', 'localhost', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);"
fi

read -p "
Task \"Create or modify mysql user\":

  This task is useful if you do not want to use Tourenplaner with the 
  root mysql user. 

  This task will grant all privileges on the de.tourenplaner.database \"$DATABASE_NAME\"
  to a mysql user. If the user does not exist, the task will create
  a new user. Therefore the task will ask you to enter the mysql user name.

  Do you want to execute this task?
  Choose yes or no (y/n): " CHOSEN_OPTION
if [ ${CHOSEN_OPTION,,} == "y" -o ${CHOSEN_OPTION,,} == "yes" ]
then

  echo "
  Please enter the username for the mysql user."
  read -p "  Username (e.g. \"tnpuser\"): " NEW_MYSQL_USER

  echo "
  Please enter the password for the mysql user $NEW_MYSQL_USER."
  while [ $NEW_MYSQL_USER_PW != $NEW_MYSQL_USER_PW_CONTROL ]
  do
    read -s -p "  Enter password: " NEW_MYSQL_USER_PW
    read -s -p "
  Repeat password:" NEW_MYSQL_USER_PW_CONTROL
    echo

    if [ $NEW_MYSQL_USER_PW != $NEW_MYSQL_USER_PW_CONTROL ]
    then
      echo "
  The entered passwords are not equal, please try again."
    fi
  done

  mysql -u $DATABASE_USER --password=$DATABASE_PASSWORD --verbose --force \
    --host=$DATABASE_HOST --port=$DATABASE_PORT <<<  "
    GRANT ALL PRIVILEGES ON $DATABASE_NAME.* TO '$NEW_MYSQL_USER'@'localhost' IDENTIFIED BY '$NEW_MYSQL_USER_PW';"

  read -p "
  Should the mysql user $NEW_MYSQL_USER also have remote access? 
  (needed if mysql de.tourenplaner.server and Tourenplaner are not on the same host)
  Choose yes or no (y/n): " CHOSEN_OPTION
  if [ ${CHOSEN_OPTION,,} == "y" -o ${CHOSEN_OPTION,,} == "yes" ]
  then
    mysql -u $DATABASE_USER --password=$DATABASE_PASSWORD --verbose --force \
      --host=$DATABASE_HOST --port=$DATABASE_PORT <<<  "
      GRANT ALL PRIVILEGES ON $DATABASE_NAME.* TO '$NEW_MYSQL_USER'@'%' IDENTIFIED BY '$NEW_MYSQL_USER_PW';"
  fi

fi

echo "
Executing de.tourenplaner.database script done.
Do not forget to edit the Tourenplaner de.tourenplaner.config file and change
within the de.tourenplaner.config file the values for the de.tourenplaner.database user, de.tourenplaner.database password
and de.tourenplaner.database url (contains the de.tourenplaner.database name).
"

