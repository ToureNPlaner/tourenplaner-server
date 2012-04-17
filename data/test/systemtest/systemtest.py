#!/usr/bin/env python3
#Copyright 2012 ToureNPlaner
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

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
