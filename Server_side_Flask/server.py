import os,pyotp
import sqlite3,json
from flask import Flask, render_template, request, redirect, url_for, send_from_directory,flash,session, abort,g
from oath import _totp
from random import randint
from flask_mail import Message,Mail


app = Flask(__name__)
app.config['MAIL_SERVER']=os.environ.get('MAIL_SERVER',          'smtp.gmail.com')
app.config['MAIL_PORT'] = int(os.environ.get('MAIL_PORT',         '465'))
app.config['MAIL_USERNAME'] =os.environ.get('MAIL_USERNAME','viethung2281996@gmail.com')
app.config['MAIL_PASSWORD'] =os.environ.get('MAIL_PASSWORD', 'xuantoan20')
app.config['MAIL_USE_TLS'] = int(os.environ.get('MAIL_USE_TLS',  False))
app.config['MAIL_USE_SSL'] = int(os.environ.get('MAIL_USE_SSL',  True))
app.config['Requires Authentication'] = int(os.environ.get('Requires Authentication',  True))
#app.config['MAIL_DEFAULT_SENDER'] = 'viethung2281996@gmail.com'
app.config.from_pyfile('config.cfg')
mail=Mail(app)


def connect_db():
	return sqlite3.connect(app.config['DATABASE'])

@app.route('/')
def index():
	return render_template('index.html')

@app.route('/login', methods=['POST'])
def login():
	#get username and password from request
	username = request.form['username']
	password = request.form['password']

	#connect database
	g.db = connect_db()
	cur = g.db.execute("select * from user")
	userList = [dict(username=row[1], password=row[2],seed=row[3] ) for row in cur.fetchall()]

	usernameList = [u.get('username') for u in userList]
	g.db.close()

	#handle
	if username not in usernameList:
		flash('Log in failed! Please heck your username or password!')
		return render_template('index.html')
	else:
		for userinput in userList:
			if userinput['username'] == username:
				if userinput['password'] != password:
					#thong bao loi
					flash('Log in failed! Please heck your username or password!')
					return render_template('index.html')

				else:
					session['seed'] = userinput['seed']
					flash('Log in successfully')
					return render_template('authenticateOTP.html')
					

@app.route('/register', methods=['POST'])
def register():
	if request.method == 'POST':
		username = request.form['username']
		email = request.form['email']
		password = request.form['password']

		#connect database
		g.db = connect_db()
		cur = g.db.execute("select * from user")
		userList = [dict(username=row[1] ) for row in cur.fetchall()]

		usernameList = [u.get('username') for u in userList]
		g.db.close()
		if username in usernameList:
			flash('Account already exists')
			return render_template('index.html')
		else:
			random_int = randint(000001,999999)
			seed = "%05d" % random_int
			#insert information to database
			g.db = connect_db()
			g.db.execute("INSERT INTO user (username,password,seed) VALUES (?,?,?)",[username,password,seed])
			g.db.commit()
			g.db.close()


			msg = Message("Seed code from server",sender="viethung2281996@gmail.com",recipients=[email])
			msg.body = seed
			mail.send(msg)
			flash('Successful registration')
			return render_template('index.html')

@app.route('/authenticate', methods=['POST'])
def authenticate():
	otpcode_client = request.form['otpcode']
	seed = session['seed']
	result = authenticateotp(seed,otpcode_client)
	if result == True:
		message = 'Authentication successfull!'
		return render_template('result.html',result = message)
	else:
		message = 'Authentication failed!'
		return render_template('result.html',result = message)


@app.route("/logout")
def logout():
	session.pop('seed', None)
	return index()

def authenticateotp(seed,otpcode_client):
	otpcode_server = _totp.totp(seed)
	if otpcode_server == otpcode_client:
		return True
	else:
		return False


if __name__ == '__main__':
	app.secret_key = os.urandom(12)
	app.run(
		host="0.0.0.0",
		port=int("10000"),
		debug=True
	)
