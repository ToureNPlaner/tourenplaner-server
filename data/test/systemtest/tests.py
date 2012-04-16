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
import base64
import urllib.parse
import sys
from tester import *


# All tests needs the explicit ordering of the users
TestUsers([
         User('usernotindbnoauth@teufel.de', 'only4testing', 'NotInDBNoAuth', 'Teufel', '', 'needs_verification', admin=False, indb=False, sendAuth=False),
         User('usernotindb@teufel.de', 'only4testing', 'NotInDB', 'Teufel','','needs_verification', admin=False, indb=False),
         User('userinactivenotadmin@teufel.de', 'only4testing', 'InactiveNotAdmin', 'Teufel','', 'needs_verification', admin=False, indb=True),
         User('userinactiveadmin@teufel.de', 'only4testing', 'InactiveAdmin', 'Teufel','', 'needs_verification', admin=True, indb=True),
         User('useractivenotadmin@teufel.de', 'only4testing', 'ActiveNotAdmin', 'Teufel','', 'verified', admin=False, indb=True),
         User('useractiveadmin@teufel.de', 'only4testing', 'ActiveAdmin', 'Teufel','', 'verified', admin=True, indb=True)
         ])
testRequestids = []
         
#All tests have as explicit ordering the implicit ordering. In the ordering they will be executed.
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
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      else:
         correct = resp['status'] == '200' and content['misc']['distance'] > 640000 and content['misc']['distance'] < 670000
         if correct:
            testRequestids.append(content['requestid'])
         return correct

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
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      else:
         return resp['status'] == '200' and content['email'] == self.user.email and content['firstname'] == self.user.firstName and content['lastname'] == self.user.lastName


@TestClass
class GetTestUserIdsTest(Test):

   def __init__(self, user):
      super().__init__(user)
      self.limit = 2**31-1
      self. offset = 0
      self.url = '/listusers?'+urllib.parse.urlencode({'limit':self.limit, 'offset': self.offset})
      self.method = 'GET'

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return status == '403' and content['errorid'] == 'ENOTADMIN'
      else:
         # zero'th user is admin with userid==1
         correct = status == '200'
         #[user if user.indb for user in testUsers]
         users = list(filter(lambda user: user.indb, testUsers))
         if correct:
            correct = False
            for i in range(content['number'], 0, -1):
               if len(users) > 0:
                  for user in users:
                     if user.email == content['users'][i-1]['email']:
                        user.userid = content['users'][i-1]['userid']
                        users.remove(user)
                        if len(users) == 0:
                           correct = True
                        break
               else:
                  correct = True
                  break
         return correct

@TestClass
class ListUserTestNormal(Test):

   def __init__(self, user):
      super().__init__(user)
      
      self.limit = 2**31-1
      self. offset = 0
      self.url = '/listusers?'+urllib.parse.urlencode({'limit':self.limit, 'offset': self.offset})
      self.method = 'GET'    
      
   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return status == '403' and content['errorid'] == 'ENOTADMIN'
      else:
         users = list(filter(lambda user: user.email == 'userinactiveadmin@teufel.de', GetTestUsers()))
         # zero'th user is admin with userid==1
         correct = status == '200'
         if correct:
            correct = False
            for i in range(content['number'], 0, -1):
               if users[0].email == content['users'][i-1]['email']:
                  correct = content['users'][i-1]['status'] == 'needs_verification'
                  correct = correct and content['users'][i-1]['admin'] == users[0].admin and content['users'][i-1]['firstname'] == users[0].firstName
                  correct = correct and content['users'][i-1]['lastname'] == users[0].lastName
             
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
      elif not self.user.status == 'verified':
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
      elif not self.user.status == 'verified':
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
      elif not self.user.status == 'verified':
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
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      else:
         correct = status == '200' and content['email'] == self.user.email and content['firstname'] == self.user.firstName
         correct = correct and content['lastname'] == self.user.lastName and content['status'] == self.user.status and content['address'] == self.user.address
         return correct


@TestClass
class GetUserWithParamTest(Test):
   users = list(filter(lambda user: user.email == 'userinactiveadmin@teufel.de', GetTestUsers()))
   def __init__(self, user):
      super().__init__(user)
      # asks for user data for user with  userinactiveadmin@teufel.de
      self.method = 'GET'
            
   def setup(self):
      self.url = '/getuser?'+urllib.parse.urlencode({'id':self.users[0].userid})
      
   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return status == '403' and content['errorid'] == 'ENOTADMIN'
      else:
         correct = status == '200' and content['email'] == self.users[0].email and content['firstname'] == self.users[0].firstName
         correct = correct and content['lastname'] == self.users[0].lastName and content['status'] == self.users[0].status
         return correct



@TestClass
class ListRequestsTestNormalWithoutId(Test):

   def __init__(self, user):
      super().__init__(user)
      self.limit = 1
      self.offset = 0
      self.url = '/listrequests?'+urllib.parse.urlencode({'limit':self.limit, 'offset': self.offset})
      self.method = 'GET'

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      else:
         correct = status == '200' and len(content['requests']) == self.limit
         correct = correct and content['requests'][0]['algorithm'] == 'sp'
         correct = correct and content['requests'][0]['status'] == 'ok'
         return correct

@TestClass
class ListRequestsTestWithoutLimitWithoutId(Test):

   def __init__(self, user):
      super().__init__(user)
      self. offset = 1
      self.url = '/listrequests?'+urllib.parse.urlencode({'offset': self.offset})
      self.method = 'GET'

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      else:
         return status == '400' and content['errorid'] == 'ELIMIT'

@TestClass
class ListRequestsTestWithoutOffsetWithoutId(Test):

   def __init__(self, user):
      super().__init__(user)
      self.limit = 1
      self.url = '/listrequests?'+urllib.parse.urlencode({'limit' :self.limit})
      self.method = 'GET'

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      else:
         return status == '400' and content['errorid'] == 'EOFFSET'

@TestClass
class ListRequestsTestWithoutParamsWithoutId(Test):

   def __init__(self, user):
      super().__init__(user)
      self. offset = 0
      self.url = '/listrequests'
      self.method = 'GET'

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      else:
         return status == '400' and (content['errorid'] == 'ELIMIT' or content['errorid'] == 'EOFFSET')


@TestClass
class ListRequestsTestNormalWithId(Test):
   users = list(filter(lambda user: user.email == 'useractivenotadmin@teufel.de', GetTestUsers()))
   
   def __init__(self, user):
      super().__init__(user)
      self.limit = 1
      self.offset = 0
      self.method = 'GET'

   def setup(self):
      self.url = '/listrequests?'+urllib.parse.urlencode({'id':self.users[0].userid ,'limit':self.limit, 'offset': self.offset})
      
   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return status == '403' and content['errorid'] == 'ENOTADMIN'
      else:
         correct = status == '200' and len(content['requests']) == self.limit
         correct = correct and content['requests'][0]['algorithm'] == 'sp'
         correct = correct and content['requests'][0]['status'] == 'ok'
         return correct

@TestClass
class ListRequestsTestWithoutLimitWithId(Test):
   users = list(filter(lambda user: user.email == 'useractivenotadmin@teufel.de', GetTestUsers()))
   
   def __init__(self, user):
      super().__init__(user)
      self. offset = 0
      self.method = 'GET'

   def setup(self):
      self.url = '/listrequests?'+urllib.parse.urlencode({'id':self.users[0].userid, 'offset': self.offset})
      
   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return (status == '403' and content['errorid'] == 'ENOTADMIN') or (status == '400' and content['errorid'] == 'ELIMIT')
      else:
         return status == '400' and content['errorid'] == 'ELIMIT'

@TestClass
class ListRequestsTestWithoutOffsetWithId(Test):
   users = list(filter(lambda user: user.email == 'useractivenotadmin@teufel.de', GetTestUsers()))

   def __init__(self, user):
      super().__init__(user)
      self.limit = 1
      self.method = 'GET'
      
   def setup(self):
      self.url = '/listrequests?'+urllib.parse.urlencode({'id':self.users[0].userid, 'limit' :self.limit})
   
   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return (status == '403' and content['errorid'] == 'ENOTADMIN') or (status == '400' and content['errorid'] == 'EOFFSET')
      else:
         return status == '400' and content['errorid'] == 'EOFFSET'

@TestClass
class ListRequestsTestWithoutParamsWithId(Test):
   users = list(filter(lambda user: user.email == 'useractivenotadmin@teufel.de', GetTestUsers()))

   def __init__(self, user):
      super().__init__(user)
      self. offset = 1
      self.method = 'GET'
   
   def setup(self):
      self.url = '/listrequests?'+urllib.parse.urlencode({'id':self.users[0].userid})
      
   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return (status == '403' and content['errorid'] == 'ENOTADMIN') or (status == '400' and content['errorid'] == 'ELIMIT')
      else:
         return status == '400' and (content['errorid'] == 'ELIMIT' or content['errorid'] == 'EOFFSET')         

@TestClass
class GetRequestWithoutId(Test):

   def __init__(self, user):
      super().__init__(user)
      self. offset = 1
      self.url = '/getrequest'
      self.method = 'GET'

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return (status == '403' and content['errorid'] == 'ENOTADMIN') or (status == '404' and content['errorid'] == 'ENOREQUESTID')
      else:
         return status == '404' and content['errorid'] == 'ENOREQUESTID'   

@TestClass
class GetRequestWithId(Test):
   
   def __init__(self, user):
      super().__init__(user)
      self. offset = 1
      self.method = 'GET'
   
   def setup(self):
      self.url = '/getrequest?'+urllib.parse.urlencode({'id':testRequestids[1]})
      
   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return status == '403' and content['errorid'] == 'ENOTADMIN'
      else:
         correct = status == '200' and len(content['points']) > 0
         return correct 
         
@TestClass
class GetResponsetWithoutId(Test):

   def __init__(self, user):
      super().__init__(user)
      self. offset = 1
      self.url = '/getresponse'
      self.method = 'GET'

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return (status == '403' and content['errorid'] == 'ENOTADMIN') or (status == '404' and content['errorid'] == 'ENOREQUESTID')
      else:
         return status == '404' and content['errorid'] == 'ENOREQUESTID'   

@TestClass
class GetResponseWithId(Test):

   def __init__(self, user):
      super().__init__(user)
      self. offset = 1
      self.method = 'GET'
      
   def setup(self):
      self.url = '/getresponse?'+urllib.parse.urlencode({'id':testRequestids[1]})

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return status == '403' and content['errorid'] == 'ENOTADMIN'
      else:
         correct = status == '200' and len(content['points']) > 0 and len(content['way']) > 0 and len(content['misc']) > 0
         return correct 
         
registeredUsers = []         
         
@TestClass
class RegisterTest(Test):

   def __init__(self, user):
         super().__init__( user)
         self.url = '/registeruser'
         self.method = 'POST'
         self.userobject = {'email' : 'registeredBy'+self.user.firstName+'@teufel.de',
         'password' : 'only4testing',
         'firstname' : 'registredBy'+self.user.firstName,
         'lastname' : 'Teufel',
         'address' : ''}
         self.request = self.userobject

   def verifyResponse(self, resp, content):
         status = resp['status']
         if not self.user.indb and self.user.sendAuth:
            return status == '401' and content['errorid'] == 'EAUTH'
         elif not self.user.status == 'verified' and self.user.sendAuth:
            return status == '403' and content['errorid'] == 'ENOTVERIFIED'
         elif not self.user.admin and self.user.sendAuth:
            return status == '403' and content['errorid'] == 'ENOTADMIN'
         else:
            correct = status == '200' and content['email'] == 'registeredBy'+self.user.firstName+'@teufel.de' and content['firstname'] == 'registredBy'+self.user.firstName
            correct = correct and content['lastname'] == self.user.lastName and (content['status'] == 'verified' if self.user.admin else content['status'] == 'needs_verification')
            if correct:
               registeredUsers.append(content)

            return correct

@TestClass
class GetUserRegisterResultTest(Test):

   def __init__(self, user):
      super().__init__(user)
      self.method = 'GET'
      
   def setup(self):
      self.url = '/getuser?'+urllib.parse.urlencode({'id':(registeredUsers[1]['userid'])})

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return status == '403' and content['errorid'] == 'ENOTADMIN'
      else:
         correct = status == '200' and content['email'] == registeredUsers[1]['email'] and content['firstname'] == registeredUsers[1]['firstname']
         correct = correct and content['lastname'] == registeredUsers[1]['lastname'] and content['status'] == registeredUsers[1]['status']
         return correct


@TestClass
class UpdateUserWithIdTest(Test):
   def __init__(self, user):
         super().__init__( user)
        
         self.method = 'POST'
         self.userobject = {'email' : 'updatedBy'+self.user.firstName+'@teufel.de',
         'password' : 'updatedonly4testing',
         'firstname' : 'updatedBy'+self.user.firstName,
         'lastname' : 'updatedTeufel',
         'address' : 'update',
         'admin' : True,
         'status' : 'needs_verification'}
         self.request = self.userobject

   def setup(self):
       self.url = '/updateuser?'+urllib.parse.urlencode({'id':(registeredUsers[1]['userid'])})

   def verifyResponse(self, resp, content):
         status = resp['status']
         if not self.user.indb:
            return status == '401' and content['errorid'] == 'EAUTH'
         elif not self.user.status == 'verified':
            return status == '403' and content['errorid'] == 'ENOTVERIFIED'
         elif not self.user.admin:
            return status == '403' and content['errorid'] == 'ENOTADMIN'
         else:
            correct = status == '200' and content['email'] == 'updatedBy'+self.user.firstName+'@teufel.de' and content['firstname'] == 'updatedBy'+self.user.firstName
            correct = correct and content['lastname'] == 'updated'+self.user.lastName and content['status'] == 'needs_verification' and content['admin']
            registeredUsers[1] = content
            return correct
            
@TestClass
class GetUserUpdateUserResultTest(Test):

   def __init__(self, user):
      super().__init__(user)
      self.method = 'GET'
      
   def setup(self):
      self.url = '/getuser?'+urllib.parse.urlencode({'id':(registeredUsers[1]['userid'])})

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return status == '403' and content['errorid'] == 'ENOTADMIN'
      else:
         correct = status == '200' and content['email'] == registeredUsers[1]['email'] and content['firstname'] == registeredUsers[1]['firstname']
         correct = correct and content['lastname'] == registeredUsers[1]['lastname'] and content['status'] == registeredUsers[1]['status']
         return correct

@TestClass
class DeleteUserWithoutIdTest(Test):

   def __init__(self, user):
      super().__init__(user)
      self.method = 'GET'
      
   def setup(self):
      self.url = '/deleteuser'

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin: 
         return status == '403' and content['errorid'] == 'ENOTADMIN' or status == '404' and content['errorid'] == 'ENOUSERID' 
      else:
         return status == '404' and content['errorid'] == 'ENOUSERID'    
         
         
@TestClass
class DeleteUserWithIdTest(Test):

   def __init__(self, user):
      super().__init__(user)
      self.method = 'GET'
      
   def setup(self):
      self.url = '/deleteuser?'+urllib.parse.urlencode({'id':(registeredUsers[1]['userid'])})

   def verifyResponse(self, resp, content):
      status = resp['status']
      if not self.user.indb:
         return status == '401' and content['errorid'] == 'EAUTH'
      elif not self.user.status == 'verified':
         return status == '403' and content['errorid'] == 'ENOTVERIFIED'
      elif not self.user.admin:
         return status == '403' and content['errorid'] == 'ENOTADMIN'
      else:
         return status == '200' and content == None

         

@TestClass
class UpdateUserWithoutIdTest(Test):
   def __init__(self, user):
         super().__init__( user)
         
         
         
         self.method = 'POST'
         self.userobject = {'email' : 'updated'+self.user.firstName+'@teufel.de',
         'password' : 'updated'+self.user.password,
         'firstname' : 'updated'+self.user.firstName,
         'lastname' : 'updated'+self.user.lastName,
         'address' : 'update',
         'admin' : not self.user.admin,
         'status' : 'needs_verification' if self.user.status == 'verified' else 'verified' }
         self.request = self.userobject

   def setup(self):
       self.url = '/updateuser'

   def verifyResponse(self, resp, content):
         status = resp['status']
         if not self.user.indb:
            return status == '401' and content['errorid'] == 'EAUTH'
         elif not self.user.status == 'verified':
            return status == '403' and content['errorid'] == 'ENOTVERIFIED'
         else:
            correct = status == '200'
            if self.user.admin:
               correct = correct and content['email'] == 'updated'+self.user.firstName+'@teufel.de' and content['firstname'] == 'updated'+self.user.firstName 
               correct = correct and content['lastname'] == 'updatedTeufel' and content['status'] == 'needs_verification' and not content['admin']
            else:
               correct = correct and content['email'] == self.user.email and content['firstname'] == self.user.firstName 
               correct = correct and content['lastname'] == self.user.lastName and content['status'] == 'verified' and not content['admin']
            return correct







