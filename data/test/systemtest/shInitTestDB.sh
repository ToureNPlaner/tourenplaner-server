#! /bin/bash

## If errors occur, next command within script will be executed (because of option --force)
## So the script will continue without error if database already exists

## You must maybe change host and port parameter if you use a remote DB

DATABASE_HOST=localhost
DATABASE_PORT=3306

echo
echo "If you are using a remote database (not at localhost) or your database"
echo uses another port, you will have to change the parameters
echo DATABASE_HOST and DATABASE_PORT within the script file.
echo

echo You will be prompted to enter a password, the password is
echo the root password of your mysql database
echo

mysql -u root -p --verbose --force \
  --host=$DATABASE_HOST --port=$DATABASE_PORT < initTestDB.sql

echo
echo Executing database script done
echo If you want to see if errors occurred, read the output above carefully.
echo

