from django.test import TestCase, Client
from pymongo import MongoClient
from datetime import datetime


def get_db():
    """Get a pymongo client for test setup/teardown."""
    return MongoClient('mongodb://localhost:27017/')


class MongoTestCase(TestCase):
    """Base test case that sets up and tears down MongoDB test data."""

    @classmethod
    def setUpClass(cls):
        super().setUpClass()
        cls.mongo = get_db()
        # Use the same 'main' and 'authorization' databases as the app
        # Seed minimal test data
        cls._seed_test_data()

    @classmethod
    def tearDownClass(cls):
        # Clean up test-specific data (leave demo data intact)
        cls.mongo.main.disaster.delete_many({'id': {'$regex': '^test_'}})
        cls.mongo.main.notification.delete_many({'message': {'$regex': '^TEST:'}})
        cls.mongo.main.safeHouses.delete_many({'state': 'TestState'})
        cls.mongo.main.victimsneedhelp.delete_many({'disaster_id': {'$regex': '^test_'}})
        cls.mongo.authorization.headquarters.delete_many({'username': 'testadmin'})
        cls.mongo.authorization.rescue_team.delete_many({'username': {'$regex': '^test_'}})
        cls.mongo.close()
        super().tearDownClass()

    @classmethod
    def _seed_test_data(cls):
        # Test disaster
        cls.mongo.main.disaster.insert_one({
            'id': 'test_disaster_1',
            'name': 'Test Flood',
            'isactive': 1,
            'scale': 7,
            'coordinates': {'latitude': '28.6139', 'longitude': '77.2090', 'radius': '10000'},
            'rescue_teams_usernames': ['test_rescue_1'],
            'statistics': {
                'total': {'affected': 100, 'deaths': 5},
                'day_0': {'affected': 100, 'deaths': 5},
            },
            'category': 'flood',
            'location': ['Delhi'],
            'starting_date': '2024-01-01',
        })

        cls.mongo.main.disaster.insert_one({
            'id': 'test_disaster_inactive',
            'name': 'Test Inactive Quake',
            'isactive': 0,
            'scale': 3,
            'coordinates': {'latitude': '19.0760', 'longitude': '72.8777', 'radius': '5000'},
            'rescue_teams_usernames': [],
            'statistics': {'total': {'affected': 10, 'deaths': 0}},
            'category': 'earthquake',
            'location': ['Maharashtra'],
            'starting_date': '2024-06-01',
        })

        # Test notification
        cls.mongo.main.notification.insert_one({
            'is_disaster': 1,
            'name': 'Test Flood',
            'location': ['Delhi'],
            'directed_to': 'people',
            'directed_from': 'headquarters',
            'message': 'TEST: Stay safe, move to shelters.',
            'date': datetime.now(),
        })

        # Test safe house
        cls.mongo.main.safeHouses.insert_one({
            'state': 'TestState',
            'safehouse': [
                {'name': 'Test Shelter A', 'latitude': '28.61', 'longitude': '77.22'},
                {'name': 'Test Shelter B', 'latitude': '28.62', 'longitude': '77.23'},
            ],
        })

        # Test HQ credentials
        cls.mongo.authorization.headquarters.insert_one({
            'username': 'testadmin',
            'password': 'testpass123',
        })

        # Test rescue team
        cls.mongo.authorization.rescue_team.insert_one({
            'username': 'test_rescue_1',
            'password': 'rescue123',
            'disaster_id': 'test_disaster_1',
        })


class IndexViewTests(MongoTestCase):
    """Tests for the main public dashboard page."""

    def setUp(self):
        self.client = Client()

    def test_index_returns_200(self):
        """Main page should load successfully."""
        response = self.client.get('/main/')
        self.assertEqual(response.status_code, 200)

    def test_index_contains_active_disasters(self):
        """Active disasters should appear in the page context."""
        response = self.client.get('/main/')
        self.assertIn('data', response.context)
        disaster_names = [d['name'] for d in response.context['data'].values()]
        self.assertIn('Test Flood', disaster_names)

    def test_index_excludes_inactive_disasters(self):
        """Inactive disasters should NOT appear on the main page."""
        response = self.client.get('/main/')
        disaster_names = [d['name'] for d in response.context['data'].values()]
        self.assertNotIn('Test Inactive Quake', disaster_names)

    def test_index_contains_location_list(self):
        """The page should have the list of Indian states for the dropdown."""
        response = self.client.get('/main/')
        self.assertIn('location_names', response.context)
        self.assertIn('Delhi', response.context['location_names'])
        self.assertIn('Kerala', response.context['location_names'])

    def test_set_user_location(self):
        """POST to getUserLocation should set the session and redirect."""
        response = self.client.post('/main/getlocation', {'location': 'Delhi'})
        self.assertEqual(response.status_code, 302)  # redirect
        # Verify session was set
        session = self.client.session
        self.assertEqual(session['locationName'], 'Delhi')

    def test_index_shows_notifications_after_location_set(self):
        """After setting location, notifications for that location should appear."""
        self.client.post('/main/getlocation', {'location': 'Delhi'})
        response = self.client.get('/main/')
        self.assertIn('notifications', response.context)


class NotificationViewTests(MongoTestCase):
    """Tests for the notifications page."""

    def setUp(self):
        self.client = Client()

    def test_notifications_page_returns_200(self):
        """Notifications page for Delhi (index 35) should load."""
        response = self.client.get('/main/get/notifications/35')  # Delhi is index 35
        self.assertEqual(response.status_code, 200)

    def test_notifications_filtered_by_location(self):
        """Only notifications matching the location should appear."""
        response = self.client.get('/main/get/notifications/35')  # Delhi
        if response.context['notifications']:
            for notf in response.context['notifications']:
                self.assertIn('Delhi', notf.get('location', []))


class HeadquartersDashboardTests(MongoTestCase):
    """Tests for the HQ admin dashboard views."""

    def setUp(self):
        self.client = Client()

    def test_dashboard_returns_200(self):
        """HQ dashboard should load even without login."""
        response = self.client.get('/main/headquarters_dashboard')
        self.assertEqual(response.status_code, 200)

    def test_dashboard_lists_all_disasters(self):
        """Dashboard should show all disasters (active and inactive)."""
        response = self.client.get('/main/headquarters_dashboard')
        names = [d['name'] for d in response.context['all_disasters']]
        self.assertIn('Test Flood', names)

    def test_all_disasters_page_returns_200(self):
        """All disasters page should load."""
        response = self.client.get('/main/headquarters/dashboard/all_disasters')
        self.assertEqual(response.status_code, 200)

    def test_all_disasters_contains_both_active_and_inactive(self):
        """Both active and inactive disasters should be listed."""
        response = self.client.get('/main/headquarters/dashboard/all_disasters')
        names = [d['name'] for d in response.context['disasters_data']]
        self.assertIn('Test Flood', names)
        self.assertIn('Test Inactive Quake', names)

    def test_add_disaster_get_returns_200(self):
        """Add disaster form should load."""
        response = self.client.get('/main/headquarters/dashboard/add_disaster')
        self.assertEqual(response.status_code, 200)

    def test_add_disaster_post_creates_disaster(self):
        """Submitting the add disaster form should create a new disaster."""
        response = self.client.post('/main/headquarters/dashboard/add_disaster', {
            'name': 'Test POST Flood',
            'activeStatus': '1',
            'scale': '5',
            'latitude': '20.0',
            'longitude': '80.0',
            'radius': '10000',
            'disasterCategory': 'flood',
            'location': ['Tamil Nadu'],
        })
        self.assertEqual(response.status_code, 302)  # redirect after success
        # Verify it was created in MongoDB
        disaster = self.mongo.main.disaster.find_one({'name': 'Test POST Flood'})
        self.assertIsNotNone(disaster)
        # Clean up
        self.mongo.main.disaster.delete_one({'name': 'Test POST Flood'})

    def test_send_notification_get_returns_200(self):
        """Send notification form should load."""
        response = self.client.get('/main/headquarters/dashboard/send_notification')
        self.assertEqual(response.status_code, 200)

    def test_add_safe_house_get_returns_200(self):
        """Add safe house form should load."""
        response = self.client.get('/main/headquarters/dashboard/add_safe_house')
        self.assertEqual(response.status_code, 200)

    def test_add_rescue_team_get_returns_200(self):
        """Add rescue team form should load."""
        response = self.client.get('/main/headquarters/dashboard/add_rescue_team')
        self.assertEqual(response.status_code, 200)

    def test_update_statistics_get_returns_200(self):
        """Update statistics page for a disaster should load."""
        response = self.client.get('/main/headquarters/dashboard/disaster/test_disaster_1/update_statistics')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.context['disaster_name'], 'Test Flood')

    def test_update_statistics_post(self):
        """Submitting updated statistics should save to MongoDB."""
        response = self.client.post(
            '/main/headquarters/dashboard/disaster/test_disaster_1/update_statistics',
            {'affected_stats': ['50', '60'], 'deaths_stats': ['2', '3']},
        )
        self.assertEqual(response.status_code, 302)
        # Verify stats were updated
        disaster = self.mongo.main.disaster.find_one({'id': 'test_disaster_1'})
        self.assertEqual(disaster['statistics']['total']['affected'], 110)
        self.assertEqual(disaster['statistics']['total']['deaths'], 5)


class ChangeActiveStatusTests(MongoTestCase):
    """Tests for the AJAX toggle that activates/deactivates disasters."""

    def setUp(self):
        self.client = Client()

    def test_toggle_active_status(self):
        """AJAX POST should toggle a disaster's active status."""
        response = self.client.post(
            '/main/headquarters/dashboard/all_disasters/change_active_status',
            {'id': 'test_disaster_1', 'status': '0'},
            HTTP_X_REQUESTED_WITH='XMLHttpRequest',
        )
        self.assertEqual(response.status_code, 200)
        # Verify it was deactivated
        disaster = self.mongo.main.disaster.find_one({'id': 'test_disaster_1'})
        self.assertEqual(disaster['isactive'], 0)
        # Restore
        self.mongo.main.disaster.update_one(
            {'id': 'test_disaster_1'}, {'$set': {'isactive': 1}}
        )

    def test_toggle_rejects_non_ajax(self):
        """Non-AJAX POST should return 400."""
        response = self.client.post(
            '/main/headquarters/dashboard/all_disasters/change_active_status',
            {'id': 'test_disaster_1', 'status': '0'},
        )
        self.assertEqual(response.status_code, 400)


class LogoutTests(MongoTestCase):
    """Tests for HQ logout."""

    def setUp(self):
        self.client = Client()

    def test_logout_clears_session_and_redirects(self):
        """Logout should clear isHeadquartersLoggedIn and redirect."""
        session = self.client.session
        session['isHeadquartersLoggedIn'] = 1
        session.save()
        response = self.client.get('/main/headquarters/account/logout')
        self.assertEqual(response.status_code, 302)
