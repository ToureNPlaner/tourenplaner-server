#!/usr/bin/env python3
import httplib2
import json
import random
import base64
import tester
import sys
import subprocess
import io
from subprocess import Popen, PIPE, STDOUT
from tests import *

def main():
   if len(sys.argv) != 6:
      print('Usage: '+sys.argv[0]+' URL databasehost databaseport datenbaseuser databasepassword')
      print('e.g. '+sys.argv[0]+ 'https://gerbera.informatik.uni-stuttgart.de:8081 gerbera.informatik.uni-stuttgart.de 3306 tourenplaner toureNPlaner')
      sys.exit(1)
   
   cmdline = ['mysql', '-u', sys.argv[4], '-p'+sys.argv[5] ,'--verbose', '--force', '--host', sys.argv[2], '--port', sys.argv[3]]
   
   #initialize DB 
   PIPE = None
   initFileSql = io.open('initTestDB.sql', 'rt', encoding='UTF-8')
   Popen(cmdline,stdin = initFileSql, stdout=PIPE)
 
   #execute tests 
   http = httplib2.Http(disable_ssl_certificate_validation=True)
   requester = tester.Requester(http)


   t = tester.Tester(sys.argv[1], requester, GetAllTests())
   t.runTests()
   
   #finalize DB
   PIPE = None
   finalFileSql = io.open('finalTestDB.sql', 'rt', encoding='UTF-8')
   Popen(cmdline,stdin = finalFileSql, stdout=PIPE)

if __name__ == "__main__":
   main()
