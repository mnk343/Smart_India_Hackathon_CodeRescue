from django.shortcuts import render
import pymongo
from pymongo import MongoClient
from django.shortcuts import get_object_or_404, render, redirect
from pprint import pprint
import os, ssl
import main

def connect():
    client = MongoClient('mongodb://localhost:27017/')
    return client

def login (request):
    print(request.session.get('isHeadquartersLoggedIn' , None))
    if request.session.get('isHeadquartersLoggedIn' , None) == 1 :
        return redirect( 'main:headquarters_dashboard' )

    if( request.method == 'POST' ):

        print( request.POST['username'] )
        client = connect()

        # if headquarters is trying to login
        db = client.authorization.headquarters
        count_authorities = db.count_documents({ 'username' :request.POST['username'] , 'password' : request.POST['password']  })
        print(count_authorities)
        if count_authorities == 1:
            request.session['isHeadquartersLoggedIn'] = 1
            return redirect( 'main:headquarters_dashboard' )

        return render( request , 'user/login_page.html' , {"error" : 1})
    return render(request, 'user/login_page.html')
