---
name: disaster-mgmt-frontend
description: Architecture and conventions for the project.
---

The project contains a web and an android app.
The web app is Django based using MongoDB as the database. We are not relying on Django ORMs for this project.
So dont run makemigrations for app data changes. The models.py are empty and it should be as is.

While development, add data in the mongoDB since the website appears empty otherwise.

There are charts displayed at the end of the dashboard. Chart.js is already loaded in index.html which is used for the pie chart. You should prefer it for any new chart work since it is well-supported. Charts.js requires <canvas> elements and not <div> containers like in other charts, make a note of that.
Note that offset:true on the xAxes helps center the points and suggestedMax on yAxes helps give offsets.

There are two different Mongo Databases, one client.main.* with app data and the other client.authorization.* with credentials for rescue team and admin.

We depend on Bing maps to find distance matrix.

The pattern while creating a new API is simple:
Check session Auth -> Connect MongoDB -> Render template.
From a code perspective:
   def connect():
      client = MongoClient('mongodb://localhost:27017/')
      return client

The Flask SOS api is a separate app independent from django. No shared code.

For testing:
Conventions:
-> Coordinates of places are stored as STRINGS and not FLOATS.
-> Location is ALWAYS an ARRAY even if there is just one location to be returned
-> Use mongomock for testing and not pymongo. It seems the latter fails repeatedly!

For notifications in Django, they are stored in client.main.notification and rendered via the index view. AJAX polling fires every 10 seconds and the session state determines whats new!

Django templates are treating 0, "", None and empty lists as false in {% if %} blocks.
