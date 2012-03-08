#!/usr/bin/env python3
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
      return {'status' : resp['status'], 'response' : json.loads(str(content, 'UTF-8'))}
         
   def setCredentials(self, name, password):      
      self.headers['authorization'] = 'Basic ' + base64.b64encode(("%s:%s" % (name, password)).encode('utf-8')).strip().decode('utf-8')
      
      
class User:
   def __init__(self, email, password, firstName, lastName, address, admin, verified):
      self.email = email
      self.password = password
      self.firstName = firstName
      self.lastName = lastName
      self.address = address
      self.admin = admin
      self.verified = verified
   
class Test:
   def __init__(self, url, method, user, request, verifyResponse):
      self.url = url
      self.method = method
      self.user = user
      self.request = request
      self.verifyResponse = verifyResponse
      
   def doTest(self, requester, baseurl):
      requester.setCredentials(self.user.email, self.user.password)
      result = requester.doRequest(baseurl+self.url, self.method, self.request)
      return self.verifyResponse(result)
      
         
class Tester:
   def __init__(self, baseurl ,requester, tests):
      self.requester = requester
      self.tests = tests
      self.baseurl = baseurl
   
   
   def runTests(self):
      for test in self.tests:
         if not test.doTest(self.requester, self.baseurl):
            print('Failed at '+str(test))

def main():
   http = httplib2.Http(disable_ssl_certificate_validation=True)
   requester = Requester(http)
   
   request = {'points':[{'lt': 487786110, 'ln' : 91794440}, {'lt': 535652780, 'ln': 100013890}]}
   verifyResponse = lambda result : result['status'] == '200' and result['response']['misc']['distance'] > 0
   user = User('root@tourenplaner.de', 'toureNPlaner', 'foo', 'bar', '', True, True)
   test = Test('/algsp', 'POST', user = user, request=request, verifyResponse = verifyResponse)
   tester = Tester('https://gerbera:8081', requester, [test])
   tester.runTests()

if __name__ == "__main__":
   main()
