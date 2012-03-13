import httplib2
import json
import random
import base64

class Requester:
   def __init__(self, http):
      self.http = http
      self.headers = {'Content-Type':'application/json', "Accept":"application/json"}

   def doRequest(self, url, method, request):
      resp, content = self.http.request(uri=url, method=method, body=json.dumps(request), headers=self.headers)
      return resp, json.loads(str(content, "UTF-8"))
   
   def setCredentials(self, name, password):
      self.headers['authorization'] = 'Basic ' + base64.b64encode(("%s:%s" % (name, password)).encode('utf-8')).strip().decode('utf-8')
   
   def noCredentials(self):
      if 'authorization' in self.headers:
         del self.headers['authorization']

class User:
   def __init__(self, email, password, firstName, lastName, address, admin, verified, indb, sendAuth = True):
      self.email = email
      self.password = password
      self.firstName = firstName
      self.lastName = lastName
      self.address = address
      self.admin = admin
      self.verified = verified
      self.indb = indb
      self.sendAuth = sendAuth

   def __str__(self):
      string = 'email: '+self.email+' pw: '+self.password+' fN: '+self.firstName+' lN: '+self.lastName
      return string+' addr: '+self.address+' (admin, verified, indb) ('+str(self.admin)+','+str(self.verified)+','+str(self.indb)+')'

class Test:
   def __init__(self, user):
      self.url = ''
      self.method = 'POST'
      self.user = user
      self.request = ''
      self.resp = None
      self.content = None

   def doTest(self, requester, baseurl):
      if self.user.sendAuth:
         requester.setCredentials(self.user.email, self.user.password)
      else:
         requester.noCredentials()

      self.resp, self.content = requester.doRequest(baseurl+self.url, self.method, self.request)
      return self.verifyResponse(self.resp, self.content)

   def verifyResponse(self, resp, content):
      pass

class Tester:
   def __init__(self, baseurl ,requester, tests):
      self.requester = requester
      self.tests = tests
      self.baseurl = baseurl

   def runTests(self):
      for test in self.tests:
         result = test.doTest(self.requester, self.baseurl)
         if not result:
            print(test.__class__.__name__+' User: '+str(test.user.email)+' ----- FAILED')
            print(str(test.resp))
            print(test.content)
            print(str(test.user))
         else:
            print(test.__class__.__name__+' User: '+str(test.user.email)+' ----- PASSED')


testClasses = []
testUsers = []



def TestClass(clazz):
   testClasses.append(clazz)
   return clazz


def TestUsers(users):
   testUsers.extend(users)

def GetAllTests():
   return [testClass(user) for testClass in testClasses for user in testUsers]

def RunsBefore(testA, testB):
   if testClasses.index(testA) > testClasses.index(testB):
      raise Exception('Test execution constraint violated for '+testA.__name__+'  and '+testB.__name__)
