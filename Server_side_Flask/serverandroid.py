import sqlite3,json
from flask import Flask, request, session, g, render_template, flash
from flask_socketio import SocketIO, emit

app = Flask(__name__)
app.config.from_pyfile('config.cfg') 
app.config['SECRET_KEY'] = 'secret!'

socketio = SocketIO(app)

def connect_db():
	return sqlite3.connect(app.config['DATABASE'])

@socketio.on('submit user')
def handle_json(user):
	print('connected!connected!connected!connected!connected!connected!')
	result = 'False'
	g.db = connect_db()
	cur = g.db.execute("select * from user")
	#get user information
	username = user['username']
	password = user['password']
	seed = user['seed']

	userList = [dict(username=row[1], password=row[2],seed=row[3] ) for row in cur.fetchall()]

	usernameList = [u.get('username') for u in userList]
	g.db.close()
	if username not in usernameList:
		print(' username wrong!')
		emit('result',{'result' : result} )
	else:
		for userinput in userList:
			if userinput['username'] == username:
				if userinput['password'] != password:
					#thong bao loi
					emit('result',{'result' : result} )
					print('password or seed wrong!')
				elif userinput['seed'] != seed:
					socketio.emit('result',{'result': result} )

				else:
					result = 'True'
					emit('result',{'result' : result} )
					print('login successful!')


if __name__ == '__main__':
	socketio.run(app, host="0.0.0.0",
		port=int("8080"),
		debug=True)