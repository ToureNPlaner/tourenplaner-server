#!/usr/bin/env python3
import httplib2
import json
import random
import time


class Requester:
   def __init__(self, http):
      self.http = http

   def doRequest(self, url, method, algSuffix, request):
      headers ={'Content-Type':'application/json', "Accept":"application/json"}
      resp, content = self.http.request(uri=url+"/alg"+algSuffix, method=method, body=json.dumps(request))
      if resp['status'] == '200':
         return True #, json.loads(str(content, "UTF-8"))
      else:
         return False #, json.loads(str(content, "UTF-8"))


def main():
   http = httplib2.Http()
   requester = Requester(http)
   
   numRequests = 0;
   startTime = time.time()
   startRequests = 0;
   request = {'points':[{'lt': 487786110, 'ln' : 91794440}, {'lt': 535652780, 'ln': 100013890}]}
   #random.seed(42)
   while True:
      points = []
      for i in range(0, 2):
         lat = random.randint(472600000, 548960000)
         lon = random.randint( 59000000, 149900000)
         points.append({'lt': lat, 'ln' : lon})
      
      request['points'] = points
      success = requester.doRequest('http://192.168.178.21:8080', 'POST','nns', request)
      if not success:
         print('Failed: '+str(content))
      else:
         numRequests+=1
         now = time.time()
         if now-startTime >= 20.0:
            num = numRequests-startRequests
            print(str(num) + ' requests in '+str(now-startTime)+' seconds')
            startTime = time.time();
            startRequests=numRequests
         #print(str(content['misc']['distance']/1000.0))

if __name__ == "__main__":
   main()
