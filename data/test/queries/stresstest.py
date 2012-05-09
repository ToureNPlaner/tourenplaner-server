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
import base64
import threading
import random
import time
import sys

test_arr = []

class TestThread(threading.Thread):
    def __init__(self, queriesPerThread, test):
        threading.Thread.__init__(self)
        self.queriesPerThread = queriesPerThread
        self.request = test.request
        self.uri = test.url
        self.test = test

    def run(self):
        http = httplib2.Http(disable_ssl_certificate_validation=True)

        headers = {'Content-Type':'application/json', "Accept":"application/json"}
        headers['authorization'] = 'Basic ' + base64.b64encode(("%s:%s" % ('root@tourenplaner.de', 'toureNPlaner')).encode('utf-8')).strip().decode('utf-8')

        self.response_arr = []

        for i in range (0, self.queriesPerThread):
            self.test.prepareTest(self)
            response, content = http.request(uri=sys.argv[1] + self.uri, method='POST', body=json.dumps(self.request), headers=headers)
            self.response_arr.append(response)

class Test():
   def __init__(self, name):
      self.name = name
      self.url = ''
      self.request = ''

   # call initialize after user has selected test but before threads are created
   # purpose: for user input or anything else which should be done only once and not for every request
   def initialize(self):
      pass

   # should be called before every single request
   # purpose: for example for random requests
   @staticmethod
   def prepareTest(testThread):
      pass

def TestAnnotation(clazz):
   test_arr.append(clazz())


@TestAnnotation
class GetResponseTest(Test):
    def __init__(self):
       super().__init__('getresponse with user input (request id)')

    def initialize(self):
       self.url = 'getresponse?id=' + str(int(input("ID of Response: ")))


@TestAnnotation
class RandomAlgSPTest(Test):
    def __init__(self):
        super().__init__('random algsp with 2 points and without user input')
        self.url = 'algsp'

    @staticmethod
    def prepareTest(testThread):
       testThread.request = {'points':[{'lt': 487786110, 'ln' : 91794440}, {'lt': 535652780, 'ln': 100013890}]}
       points = []
       for i in range(0, 2):
          lat = random.randint(472600000, 548960000)
          lon = random.randint( 59000000, 149900000)
          points.append({'lt': lat, 'ln' : lon})
       testThread.request['points'] = points



@TestAnnotation
class ExtremeShortAlgSPTest(Test):
    def __init__(self):
        super().__init__('extreme short algsp without user input')
        self.url = 'algsp'
        self.request = {'points':[{'lt': 487131064, 'ln' : 92573199}, {'lt': 487129407, 'ln': 92572314}]}


@TestAnnotation
class GetUserWithInputTest(Test):
    def __init__(self):
       super().__init__('getuser with user input (user id)')

    def initialize(self):
       self.url = 'getuser?id=' + str(int(input("ID of User: ")))


@TestAnnotation
class GetUserWithoutInputTest(Test):
    def __init__(self):
        super().__init__('getuser without user input')
        self.url = 'getuser'

def main():

   if len(sys.argv) < 4:
      print("Parameter 1: url\nParameter 2: # of test\n\t" + "\n\t".join(map(lambda x: "" + str(test_arr.index(x)) + ": " + x.name, test_arr)) + "\nParameter 3: # threads\nParameter 4: # queries")
      return 1

   chosen_test = int(sys.argv[2])

   test = test_arr[chosen_test]
   test.initialize()

   max = int(sys.argv[3])
   queries = int(sys.argv[4])

   thread_arr = []

   i = max

   while i > 0:
      thread = TestThread(queries, test)
      thread_arr.append(thread)
      i -= 1

   start = time.time()

   for t in thread_arr:
      t.start()

   for t in thread_arr:
      t.join()

   end = time.time()

   failure = 'false'
   failure_cnt = 0

   for t in thread_arr:

      for response in t.response_arr:
        if not response['status'] == '200':
            if failure == 'false':
                #print(str(t.content, 'UTF-8') + '\n\nCould not compute all threads, error code: ' + response['status'])
                print('\n\nCould not compute all threads, error code: ' + response['status'])
            failure = 'true'
            failure_cnt += 1

   if failure == 'false':
      print('All threads computed (' + str(max) + '), all queries computed (' + str(max*queries) + ')')
   else:
      print('Failures: ' + str(failure_cnt) + ' / ' + str(max*queries))

   
   print('Time needed: ' + str(end - start) + ' s')
   print('QPS = '+str(max*queries/(end - start)))

if __name__ == "__main__":
   main()
