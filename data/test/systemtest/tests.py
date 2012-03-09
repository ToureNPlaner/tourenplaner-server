#!/usr/bin/env python3
import httplib2
import base64
from tester import Test
from tester import User


class SPTest(Test):
   def __init__(self, user):
      super().__init__(user)
      self.url = '/algsp'
      # Stuttgart to Hamburg
      self.request = {'points':[{'lt': 487786110, 'ln' : 91794440}, {'lt': 535652780, 'ln': 100013890}]}
   
   def verifyResponse(self, resp, content):
      # The distance should be 640 km < distance < 670 km
      status = resp['status']      
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.verified:
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      else:
         return resp['status'] == '200' and content['misc']['distance'] > 640000 and content['misc']['distance'] < 670000
      


users = [
         User('usernotindb@teufel.de', 'only4testing', 'NotInDB', 'Teufel','', admin=False, verified=False, indb=False),
         User('userinactivenotadmin@teufel.de', 'only4testing', 'InactiveNotAdmin', 'Teufel','', admin=False, verified=False, indb=True),
         User('userinactiveadmin@teufel.de', 'only4testing', 'InactiveAdmin', 'Teufel','', admin=True, verified=False, indb=True),
         User('useractivenotadmin@teufel.de', 'only4testing', 'ActiveNotAdmin', 'Teufel','', admin=False, verified=True, indb=True),
         User('useractiveadmin@teufel.de', 'only4testing', 'ActiveAdmin', 'Teufel','', admin=True, verified=True, indb=True)]
         
testClasses = [SPTest]
allTests = [testClass(user) for testClass in testClasses for user in users]

         
