from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect
from django.urls import reverse

def index(request):
    context = {}
    return render(request , 'main/index.html' , context)


#mayank code starts here... kindly accept my part of code if merge conflict arises

def headquarters_dashboard(request):
    return render( request , 'headquarters/dashboard.html' )

def rescue_team_dashboard(request):
    return render( request , 'rescue_team/dashboard.html' )
