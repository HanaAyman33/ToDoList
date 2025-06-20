import datetime
import sqlite3, traceback
from flask import Blueprint, Flask, redirect, render_template, jsonify, request, session, g, url_for # type: ignore
from flask_jwt_extended import JWTManager,create_access_token,jwt_required, get_jwt_identity, get_jwt # type: ignore

app = Flask(__name__)
app.secret_key = 'your_secret_key' 
app.template_folder = 'templates' 
app.config['JWT_SECRET_KEY'] = 'your_jwt_secret_key'  # JWT secret key 
app.config['JWT_TOKEN_LOCATION'] = ['headers']  # Ensure JWT is taken from headers
app.config['JWT_HEADER_NAME'] = 'Authorization'
app.config['JWT_HEADER_TYPE'] = 'Bearer'
app.config['JWT_ACCESS_TOKEN_EXPIRES'] = datetime.timedelta(minutes=1)  # 24-hour token validity
jwt = JWTManager(app)  # Initialize Flask-JWT-Extended

# Login Blueprint
login_bp = Blueprint('login',__name__, url_prefix='/auth')

# Connecting to the database    
DATABASE = '/Users/hanaayman/AndroidStudioProjects/TDList.db'

def get_db():
    db = getattr(g, '_database', None)  # Check if a connection exists
    if db is None:
        print("Creating a new database connection") # Add a log
        db = g._database = sqlite3.connect(DATABASE) # Create connection only if it doesn't exist
    return db

@app.teardown_appcontext
def close_connection(exception):
    db = getattr(g, '_database', None)
    if db is not None:
        print("Closing the database connection") # Add a log
        db.close()

@app.route('/welcome/', methods=['GET'])
def welcome():
    with app.app_context():
        print("Get Request received \n")
        print("Welcome page accessed")
        return render_template("welcome.html")  

@login_bp.route('/login/', methods=['GET','POST'])
def login():
    with app.app_context():
        with get_db() as conn: 
            if request.method == 'GET':
                print("Get Request recieved")  # Check if the request is reaching Flask
                return render_template("activity_main.html")
            else:
                try:
                    print("Post Request received")  # Check if the request is reaching Flask
                    cur = conn.cursor()
                    username = request.form.get('username','').strip()
                    password = request.form.get('password','').strip()

                    print(f"Username: {username}")
                    print(f"Password: {password}")
                    
                    # Check if this user has an account
                    cursor = cur.execute('SELECT id FROM users WHERE username = ? AND password = ?', (username, password))
                    user = cursor.fetchone()
                    if username == "" or password == "": # Handle empty username and password entries
                        print("fail")
                        return jsonify({'status': 'failure', 'message': 'Empty username or password'})
                    if user:   # Handle successful login
                        print("Login successful")
                        user_id = user[0]
                        expires = datetime.timedelta(minutes=1)  # Explicitly set token expiration
                        access_token = create_access_token(identity=str(user_id), expires_delta=expires)
                
                        print("Access Token:", access_token)  # Log for debugging
                        return jsonify({'status': 'success', 'message': 'Login successful','access_token': access_token,'expires_in': 86400})   # Expiration time in seconds (24 hours)
                    else:
                        print("Login fail")
                        return jsonify({'status': 'failure', 'message': 'Invalid username or password.'}), 401
                        
                except Exception as e:
                    conn.rollback()  # Rollback any changes in case of an error
                    print(f"Error during login: {e}")
                    return jsonify({'status': 'error', 'message': str(e)}), 500 

@app.route('/register/', methods=['GET','POST'])
def register():
    with app.app_context():
        with get_db() as conn:
            if request.method == 'GET':
                print("Get Request recieved")
                return render_template("layout2.html")
            else:
                try:
                    print("Post Request recieved")
                    cur = conn.cursor()
                    username = request.form.get('username', '').strip() 
                    password = request.form.get('password', '').strip()
    
                    cur.execute('SELECT id FROM users WHERE username = ?', (username,))
                    existing_user = cur.fetchone()
                    # Handle empty username and password entries
                    if username == "" or password == "":
                        print("fail")
                        return jsonify({'status': 'failure', 'message': 'Invalid username or password'})
                    # Check if the username already exists
                    if existing_user:
                        print("Register Failed")
                        return jsonify({'status': 'failure', 'message': 'Username already exists'})

                    # If username doesn't exist, insert the new user
                    cur.execute('INSERT INTO users (username, password) VALUES (?, ?)', (username, password))
                    conn.commit()
                    print("Registration successful")
                    return jsonify({'status': 'success', 'message': 'Registeration successful'})

                except sqlite3.IntegrityError: 
                    # Handle the specific IntegrityError 
                    conn.rollback()  
                    return jsonify({'status': 'failure', 'message': 'Username already exists'}) 

                except Exception as e:
                    conn.rollback()  
                    return jsonify({'status': 'error', 'message': f'An error occurred during registration: {e}'}), 500 

@app.route('/To-Do-List/', methods=['GET', 'POST', 'PUT'])
@jwt_required() # This checks for the presence of a JWT
def todo_list():
    # with app.app_context():
        print("Request Headers:", request.headers)  # Debugging header issues
        user_id = int(get_jwt_identity())   # get the user id and cast it to an integer (for sql queries)
        print("User ID: ", user_id)
        with get_db() as conn:
            try:
                print("Request received:", request.method, request.url) # Debugging :)
                cur = conn.cursor()
                if request.method == 'GET': # Handle Get Request
                    print("Get Request recieved")
                    cur.execute('SELECT task FROM tasks WHERE userID = ?', (user_id,))
                    tasks = [row[0] for row in cur.fetchall()]
                    print("Fetched tasks:", tasks)  # Display output "Debugging :)"
                    return jsonify({'status': 'success', 'tasks': tasks})

                elif request.method == 'POST':  # Handle Post Request
                    #try:
                        print("Post Request recieved")
                        print("userID: ", user_id)  # Print to check the value
                        data = request.get_json()   # None if json is invalid
                        new_task = data.get('task', '').strip()
                        if not new_task:    # Handling empty tasks
                            return jsonify({'status': 'error', 'message': 'Task cannot be empty'}), 400
                        
                        # Check if the task already exists for the user
                        cur.execute('SELECT 1 FROM tasks WHERE userID = ? AND task = ?', (user_id, new_task))
                        existing_task = cur.fetchone()
                        if existing_task:
                            return jsonify({'status': 'error', 'message': 'Task already exists. Get it done!'}), 400

                        # Insert the new task to db
                        cur.execute('INSERT INTO tasks (userID, task) VALUES (?, ?)', (user_id, new_task))  
                        conn.commit()
                        print("Task added")
                        return jsonify({'status': 'success', 'message': 'Task added successfully'})

                elif request.method == 'PUT':  # Handle Put Request
                    print("Put Request recieved")
                    data = request.get_json(force=True, silent=True)
                    task = data.get('task', '').strip()
                    if not task:
                        return jsonify({'status': 'error', 'message': 'Missing "task" entry'}), 400
                    # Delete the task from db
                    cur.execute('DELETE FROM tasks WHERE task=? and userID=?', (task, user_id))
                    conn.commit()
                    print("Deleting task: {task} for user: {user_id}")
                    return jsonify({'status': 'success', 'message': 'Done!'})

            except Exception as e:
                print("Error:", e)  # Print the full error for debugging
                traceback.print_exc()  # Log the full traceback
                return jsonify({'status': 'error', 'message': str(e)}), 500

app.register_blueprint(login_bp)

if __name__ == "__main__":
    app.run(debug=True)