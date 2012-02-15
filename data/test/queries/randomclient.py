#!/usr/bin/env python3
import httplib2
import json
import random

def doRequest(http,url, algSuffix, request):
   headers ={'Content-Type':'application/json', "Accept":"application/json"}
   resp, content = http.request(uri=url+"/alg"+algSuffix, method="POST", body=json.dumps(request))
   if resp['status'] == '200':
      return True, json.loads(str(content, "UTF-8"))
   else:
      return False, json.loads(str(content, "UTF-8"))


def main():
   http = httplib2.Http()
   request = {'points':[{'lt': 487786110, 'ln' : 91794440}, {'lt': 535652780, 'ln': 100013890}]}
   random.seed(42)
   for i in range(0, 10):
      points = []
      for i in range(0, 16):
         lat = random.randint(472600000, 548960000)
         lon = random.randint( 59000000, 149900000)
         points.append({'lt': lat, 'ln' : lon})
      
      request['points'] = points
      success, content = doRequest(http, 'http://localhost:8080', 'tsp', request)
      if not success:
         print('Failed: '+content)
      else:
         print(str(content['misc']['distance']/1000.0))

if __name__ == "__main__":
   main()
