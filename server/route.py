import os
import sys
from flask import Flask

app = Flask(__name__)

@app.route('/')
def hello_world():
    return 'Welcome to Splitwisr!'

@app.route('/add', method=['PUT'])
def add_receipts():
    # TODO
    return 'PLACEHOLDER'

@app.route('/receipts', methods=['GET'])
def retrieve_receipts():
    # TODO
    return 'PLACEHOLDER'