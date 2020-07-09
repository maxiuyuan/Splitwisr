# app.py

# Required imports
import os
from flask import Flask, request, jsonify
from firebase_admin import credentials, firestore, initialize_app

# Initialize Flask app
app = Flask(__name__)

# Initialize Firestore DB
cred = credentials.Certificate('ece452-297ff-firebase-adminsdk-o4qg7-a1594d91b9.json')
default_app = initialize_app(cred)
db = firestore.client()
receipt_ref = db.collection('Recepit')

@app.route('/add', methods=['POST'])
def create():
    """
        create() : Add document to Firestore collection with request body.
        Ensure you pass a custom ID as part of json body in post request,
        e.g. json={'id': '1', 'title': 'Write a blog post'}
    """
    try:
        id = request.json['id']
        receipt_ref.document(id).set(request.json)
        return jsonify({"success": True}), 200
    except Exception as e:
        return f"An Error Occured: {e}"

@app.route('/list', methods=['GET'])
def read():
    """
        read() : Fetches documents from Firestore collection as JSON.
        todo : Return document that matches query ID.
        all_todos : Return all documents.
    """
    try:
        # Check if ID was passed to URL query
        todo_id = request.args.get('id')
        if todo_id:
            todo = receipt_ref.document(todo_id).get()
            return jsonify(todo.to_dict()), 200
        else:
            all_todos = [doc.to_dict() for doc in receipt_ref.stream()]
            return jsonify(all_todos), 200
    except Exception as e:
        return f"An Error Occured: {e}"

@app.route('/update', methods=['POST', 'PUT'])
def update():
    """
        update() : Update document in Firestore collection with request body.
        Ensure you pass a custom ID as part of json body in post request,
        e.g. json={'id': '1', 'title': 'Write a blog post today'}
    """
    try:
        id = request.json['id']
        receipt_ref.document(id).update(request.json)
        return jsonify({"success": True}), 200
    except Exception as e:
        return f"An Error Occured: {e}"

@app.route('/delete', methods=['GET', 'DELETE'])
def delete():
    """
        delete() : Delete a document from Firestore collection.
    """
    try:
        # Check for ID in URL query
        todo_id = request.args.get('id')
        receipt_ref.document(todo_id).delete()
        return jsonify({"success": True}), 200
    except Exception as e:
        return f"An Error Occured: {e}"

@app.route('/', methods=['GET'])
def read_only():
    # try:
        # Check if ID was passed to URL query
        # todo_id = request.args.get('id')
        # if todo_id:
        todo = receipt_ref.document('receipt_abc_1').get()
        print('asd')
        print(todo)
        return jsonify(todo.to_dict()), 200
        # else:
        #     all_todos = [doc.to_dict() for doc in receipt_ref.stream()]
        #     return jsonify(all_todos), 200
    # except Exception as e:
    #     return f"An Error Occured: {e}"

# Adding a new receipt
    # Break them down to each transaction (per user)

# Retreiving the (unpaid) Tx and balance between two users
    # Filter for all unflagged Tx between the two users
    # List all the items and balance between the two user
    # Sum out total balance

# Updating (paying for) Tx between two users
    # Update flag in the appropriate Tx to eliminate balance

port = int(os.environ.get('PORT', 8080))
if __name__ == '__main__':
    app.run(threaded=True, host='0.0.0.0', port=port)
