#!/usr/bin/env python3
import httplib2
import json
import base64
import threading

test_factory_arr = []

class TestThread(threading.Thread):
    def __init__(self, queriesPerThread, test):
        threading.Thread.__init__(self)
        self.queriesPerThread = queriesPerThread
        self.request = test.request
        self.uri = test.url

    def run(self):
        http = httplib2.Http(disable_ssl_certificate_validation=True)

        headers = {'Content-Type':'application/json', "Accept":"application/json"}
        headers['authorization'] = 'Basic ' + base64.b64encode(("%s:%s" % ('root@tourenplaner.de', 'toureNPlaner')).encode('utf-8')).strip().decode('utf-8')

        self.response_arr = []

        for i in range (0, self.queriesPerThread):
            response, content = http.request(uri='https://gerbera:8081/' + self.uri, method='POST', body=json.dumps(self.request), headers=headers)
            self.response_arr.append(response)

class Test():
   def __init__(self, name, url, request):
      self.name = name
      self.url = url
      self.request = request

def TestFactoryAnnotation(clazz):
    for i in range(clazz.varcount):
        test_factory_arr.append(clazz(i))

class TestFactory():

   varcount = 1

   def __init__(self, var):
      self.var = var
      self.name = 'abstract test factory'

   def createTest(self):
       pass

@TestFactoryAnnotation
class GetResponseTestFactory(TestFactory):
    def __init__(self,var):
        super().__init__(var)
        self.name = 'getresponse with user input (request id)'

    def createTest(self):
        return Test(self.name, 'getresponse?id=' + str(int(input("ID of Response: "))), '')

@TestFactoryAnnotation
class ShortAlgSPTestFactory(TestFactory):
    def __init__(self, var):
        super().__init__(var)
        self.name = 'short alg sp without user input'

    def createTest(self):
        return Test(self.name, 'algsp', {'points':[{'lt': 487131064, 'ln' : 92573199}, {'lt': 487129407, 'ln': 92572314}]})

@TestFactoryAnnotation
class GetUserTestFactory(TestFactory):

    varcount = 2

    def __init__(self, var):
        super().__init__(var)
        if var == 0:
            self.name = 'getuser with user input (user id)'
        else:
            self.name = 'getuser without user input'

    def createTest(self):
        if self.var == 0:
            return Test(self.name, 'getuser?id=' + str(int(input("ID of User: "))), '')
        else:
            return Test(self.name, 'getuser', '')

def main():

   test_arr = test_factory_arr

   i = 0
   for t in test_arr:
      print(str(i) + '\t' + t.name)
      i += 1

   chosen_test = int(input("\nChoose your test: "))

   test = test_arr[chosen_test].createTest()

   max = int(input("Max number of threads: "))
   queries = int(input("Number of queries per thread: "))

   thread_arr = []

   i = max

   while i > 0:
      thread = TestThread(queries, test)
      thread_arr.append(thread)
      i -= 1

   for t in thread_arr:
      t.start()

   for t in thread_arr:
      t.join()

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


if __name__ == "__main__":
   main()
