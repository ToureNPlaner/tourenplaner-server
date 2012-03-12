#!/usr/bin/env python3
import httplib2
import json
import random
import base64
import tester
import sys
from tests import *

def main():
   if len(sys.argv) != 2:
      print('Usage: '+sys.argv[0]+' URL')
      print('e.g. '+sys.argv[0]+ 'https://gerbera.informatik.uni-stuttgart.de:8081')
      sys.exit(1)

   http = httplib2.Http(disable_ssl_certificate_validation=True)
   requester = tester.Requester(http)


   t = tester.Tester(sys.argv[1], requester, GetAllTests())
   t.runTests()

if __name__ == "__main__":
   main()
