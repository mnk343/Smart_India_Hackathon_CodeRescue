---
name: disaster-mgmt-frontend
description: Architecture, conventions, and testing patterns for the Smart India Hackathon disaster management project.
---

## Architecture

The project contains a web app (Django) and an Android app. The Flask SOS API (`web/sosEndpointApi/`) is a separate app independent from Django with no shared code.

The web app is Django-based using **MongoDB** as the database. We do NOT use Django ORMs for app data. Do not run `makemigrations` for app data changes. `models.py` files are empty and should stay that way.

## MongoDB Structure

Two databases, both accessed via `pymongo.MongoClient('mongodb://localhost:27017/')`:

**`client.main.*`** (app data):
- `disaster` — disaster records (`id`, `name`, `isactive`, `scale`, `coordinates`, `category`, `location`, `statistics`, `rescue_teams_usernames`, `starting_date`)
- `notification` — notifications (`is_disaster`, `name`, `location`, `directed_to`, `directed_from`, `message`, `date`)
- `safeHouses` — safe houses grouped by state (`state`, `safehouse[]` each with `name`, `latitude`, `longitude`)
- `victimsneedhelp` — victim help requests

**`client.authorization.*`** (credentials):
- `headquarters` — admin login (`username`, `password`)
- `rescue_team` — rescue team login (`username`, `password`, `disaster_id`)

## API Pattern

Every view follows: Check session auth → Connect MongoDB → Query → Render template.

```python
def connect():
    client = MongoClient('mongodb://localhost:27017/')
    return client
```

We depend on Bing Maps API for distance matrix calculations (safe house proximity).

## Data Conventions

- Coordinates are stored as **STRINGS**, not floats (e.g., `'28.6139'`)
- `location` is **ALWAYS an array** even for a single location (e.g., `['Delhi']`)
- `isactive` is an **integer** (1 = active, 0 = inactive), not a boolean

## Testing with mongomock

**Do NOT use a real MongoDB server in test environments.** The `mongod` process fails to start reliably in cloud/container environments (VMVM, Harbor). Use `mongomock` instead.

### How it works

`mongomock` is a pure-Python in-memory drop-in replacement for `pymongo`. It intercepts `MongoClient()` calls so the views work without any code changes.

### MongoTestCase base class pattern

```python
import mongomock
from django.test import TestCase, Client

@mongomock.patch(servers=(('localhost', 27017),))
class MongoTestCase(TestCase):
    @classmethod
    def setUpClass(cls):
        super().setUpClass()
        cls.mongo = MongoClient('mongodb://localhost:27017/')
        cls._seed_test_data()

    @classmethod
    def tearDownClass(cls):
        cls.mongo.close()
        super().tearDownClass()

    @classmethod
    def _seed_test_data(cls):
        # Insert test data into the in-memory mock
        cls.mongo.main.disaster.insert_one({...})
        cls.mongo.main.safeHouses.insert_one({...})
        # etc.
```

The `@mongomock.patch()` decorator intercepts ALL `pymongo.MongoClient('localhost', 27017)` calls — including those in views.py's `connect()` function — so the entire app uses the in-memory mock transparently.

### Supported operations (all used in this project)

`find()`, `find_one()`, `insert_one()`, `insert_many()`, `update_one()` with `$set`, `delete_one()`, `delete_many()` with `$regex`, `count_documents()`, `sort()`, `drop()`, `close()` — all supported by mongomock.

### For SWE-bench task Dockerfiles

```dockerfile
# Do NOT install mongodb-org. Instead:
RUN pip install --no-cache-dir mongomock
```

### For test.sh

Do NOT start `mongod`. Skip the "Ensure MongoDB is running" block entirely. The `@mongomock.patch()` decorator handles everything in-process.
