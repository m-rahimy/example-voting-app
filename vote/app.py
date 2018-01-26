from flask import Flask, render_template, request, make_response, g
from redis import Redis

import psycopg2
import sys
import time
import os
import socket
import random
import json

def init_db():
    connection_string = "host='db' dbname='postgres' user='postgres' password='theCamelsHateUs port=5858' "
    print "DEBUG ::: Connecting to database\n    ->%s" % (connection_string)
    while True:
      try:
          conn = psycopg2.connect(connection_string)
          break
      except psycopg2.OperationalError as err:
          print "DEBUG ::: psycopg2.connect error: {0} ".format(err)
          time.sleep(1)
    cursor = conn.cursor()
    #cursor.execute("SELECT 1;")
    #print cursor.fetchone()
    #cursor.execute("SELECT * FROM people;")
    #print cursor.fetchone()
    sql = "SELECT candidate.id AS op1ID, candidate.name AS op1Name, A.id AS op2ID, A.name AS op2Name                                   FROM                                      candidate A                                   JOIN candidate                                   ON candidate.id = A.opponent_id                                WHERE candidate.id < A.id;"
    cursor.execute(sql)
    row = cursor.fetchone()
    print "fetching candidates"
    opts = []
    while row is not None:
        opt = [{'id':row[0], 'name':row[1]}, { 'id': row[2] , 'name':row[3] }]
        print "opt: {0} " .format(opt)
        opts.append(opt)
        row = cursor.fetchone()
    print "closing the cursor"
    cursor.close()
    print "DEBUG ::: Connected! "
    return opts

# TODO: WE NEED TO add database key for these array
name = 'name'
_id = 'id'
#options =[[{_id:1,name:"Cats"}, {_id:2, name:"Dogs"}] , [{_id:3,name:"Apple"}, {_id:4,name:"Android"}] , [{_id:5,name:"Pepsi"}, {_id:6,name:"Coca Cola"}], [{_id:7,name:"Blue Pen"}, {_id:8,name:"Black Pen"}], [{_id:9,name:"Regular Pen"},{_id:10,name:"Fountain Pen"}], [{_id:11,name:"Pencil"},{_id:12,name:"Mechanical Pencil"}] ]

options = init_db();
print options

hostname = socket.gethostname()

app = Flask(__name__)

def get_redis():
    if not hasattr(g, 'redis'):
        g.redis = Redis(host="redis", db=0, socket_timeout=5)
    return g.redis

@app.route("/", methods=['POST','GET'])
def hello():
    voter_id = request.cookies.get('voter_id')
    if not voter_id:
        voter_id = hex(random.getrandbits(64))[2:-1]

    vote = None

    if request.method == 'POST':
        redis = get_redis()
        vote = request.form['vote']
        #print request.headers.get('User-Agent')
        print request.access_route
        
        for i in request.headers:
            #print "K: {0}, \n V: {1} \n\n ".format(i ,request.headers[i])
            print "K: {0}, \n ".format(i)
        
        print "DEBUG ::: received vote : " + vote
        # todo: add other elements here
        data = json.dumps({'voter_id': voter_id, 'vote': vote})
        redis.rpush('votes', data)

    resp = make_response(render_template(
        'index.html',
        options=options,
        hostname=hostname,
        vote=vote,
    ))
    resp.set_cookie('voter_id', voter_id)
    return resp


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=80, debug=True, threaded=True)
