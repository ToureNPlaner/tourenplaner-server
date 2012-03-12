#!/usr/bin/env python3
import httplib2
import base64
import urllib.parse
from tester import *

@TestClass
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

@TestClass
class AuthTest(Test):

   def __init__(self, user):
      super().__init__(user)
      self.url = '/authuser'
      self.method = 'GET'

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.verified:
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      else:
         return resp['status'] == '200' and content['email'] == self.user.email and content['firstname'] == self.user.firstName and content['lastname'] == self.user.lastName


@TestClass
class ListUserTestNormal(Test):

   def __init__(self, user):
      super().__init__(user)
      self.limit = 4
      self. offset = 1
      self.url = '/listusers?'+urllib.parse.urlencode({'limit':self.limit, 'offset': self.offset})
      self.method = 'GET'

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.verified:
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return status == '403' and content['errorid'] == 'ENOTADMIN'
      else:
         # zero'th user is admin with userid==1
         correct = status == '200' and len(content['users']) == self.limit
         correct = correct and content['users'][0]['email'] == 'userinactivenotadmin@teufel.de'
         correct = correct and content['users'][0]['status'] == 'needs_verification' and content['users'][1]['email'] == 'userinactiveadmin@teufel.de'
         return correct

@TestClass
class ListUserTestWithoutLimit(Test):

   def __init__(self, user):
      super().__init__(user)
      self. offset = 1
      self.url = '/listusers?'+urllib.parse.urlencode({'offset': self.offset})
      self.method = 'GET'

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.verified:
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return status == '403' and content['errorid'] == 'ENOTADMIN'
      else:
         return status == '400' and content['errorid'] == 'ELIMIT'

@TestClass
class ListUserTestWithoutOffset(Test):

   def __init__(self, user):
      super().__init__(user)
      self.limit = 4
      self.url = '/listusers?'+urllib.parse.urlencode({'limit' :self.limit})
      self.method = 'GET'

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.verified:
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return status == '403' and content['errorid'] == 'ENOTADMIN'
      else:
         return status == '400' and content['errorid'] == 'EOFFSET'

@TestClass
class ListUserTestWithoutParams(Test):

   def __init__(self, user):
      super().__init__(user)
      self. offset = 1
      self.url = '/listusers'
      self.method = 'GET'

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.verified:
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return status == '403' and content['errorid'] == 'ENOTADMIN'
      else:
         return status == '400' and (content['errorid'] == 'ELIMIT' or content['errorid'] == 'EOFFSET')

@TestClass
class GetUserNoParamTest(Test):

   def __init__(self, user):
      super().__init__(user)
      # asks for own user data
      self.url = '/getuser'
      self.method = 'GET'

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.verified:
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      else:
         correct = status == '200' and content['email'] == self.user.email and content['firstname'] == self.user.firstName
         correct = correct and content['lastname'] == self.user.lastName and content['status'] == 'verified' and content['address'] == self.user.address
         return correct


@TestClass
class GetUserWithParamTest(Test):

   def __init__(self, user):
      super().__init__(user)
      # asks for user data for user with userid==3 which in the test db is userinactiveadmin@teufel.de
      self.userid = 2
      self.url = '/getuser?'+urllib.parse.urlencode({'id':3})
      self.method = 'GET'

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.verified:
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return status == '403' and content['errorid'] == 'ENOTADMIN'
      else:
         correct = status == '200' and content['email'] == 'userinactiveadmin@teufel.de' and content['firstname'] == 'InactiveAdmin'
         correct = correct and content['lastname'] == 'Teufel' and content['status'] == 'needs_verification'
         return correct


TestUsers([
         User('usernotindb@teufel.de', 'only4testing', 'NotInDB', 'Teufel','', admin=False, verified=False, indb=False),
         User('userinactivenotadmin@teufel.de', 'only4testing', 'InactiveNotAdmin', 'Teufel','', admin=False, verified=False, indb=True),
         User('userinactiveadmin@teufel.de', 'only4testing', 'InactiveAdmin', 'Teufel','', admin=True, verified=False, indb=True),
         User('useractivenotadmin@teufel.de', 'only4testing', 'ActiveNotAdmin', 'Teufel','', admin=False, verified=True, indb=True),
         User('useractiveadmin@teufel.de', 'only4testing', 'ActiveAdmin', 'Teufel','', admin=True, verified=True, indb=True)
         ])



