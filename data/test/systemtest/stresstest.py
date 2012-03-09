#!/usr/bin/env python3
import httplib2
import json
import base64
import threading

class TestThread(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)

    def run(self):
        http = httplib2.Http(disable_ssl_certificate_validation=True)

        request = {'points':[{'lt': 487131064, 'ln' : 92573199}, {'lt': 487129407, 'ln': 92572314}]}
        headers = {'Content-Type':'application/json', "Accept":"application/json"}
        headers['authorization'] = 'Basic ' + base64.b64encode(("%s:%s" % ('root@tourenplaner.de', 'toureNPlaner')).encode('utf-8')).strip().decode('utf-8')

        self.response, self.content = http.request(uri='https://gerbera:8081/algsp', method='POST', body=json.dumps(request), headers=headers)

def main():

   max = int(input("Max number of threads: "))

   thread_arr = []

   i = max
   while i > 0:
      thread = TestThread()
      thread_arr.append(thread)
      i -= 1

   for t in thread_arr:
      t.start()

   for t in thread_arr:
      t.join()

   failure = 'false'
   failure_cnt = 0

   for t in thread_arr:

      if not t.response['status'] == '200':
         if failure == 'false':
            print(str(t.content, 'UTF-8') + '\n\nCould not compute all threads, error code: ' + t.response['status'])
         failure = 'true'
         failure_cnt += 1

   if failure == 'false':
      print('All threads computed (' + str(max) + ')')
   else:
      print('Failures: ' + str(failure_cnt) + ' / ' + str(max))


if __name__ == "__main__":
   main()
