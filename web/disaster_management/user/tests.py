from django.test import TestCase, Client
from pymongo import MongoClient


def get_db():
    return MongoClient('mongodb://localhost:27017/')


class LoginTests(TestCase):
    """Tests for HQ login flow."""

    @classmethod
    def setUpClass(cls):
        super().setUpClass()
        cls.mongo = get_db()
        cls.mongo.authorization.headquarters.insert_one({
            'username': 'testlogin',
            'password': 'testpass',
        })

    @classmethod
    def tearDownClass(cls):
        cls.mongo.authorization.headquarters.delete_many({'username': 'testlogin'})
        cls.mongo.close()
        super().tearDownClass()

    def setUp(self):
        self.client = Client()

    def test_login_page_returns_200(self):
        """GET /user/login should show the login page."""
        response = self.client.get('/user/login/')
        self.assertEqual(response.status_code, 200)

    def test_login_with_valid_credentials(self):
        """POST with correct username/password should redirect to HQ dashboard."""
        response = self.client.post('/user/login/', {
            'username': 'testlogin',
            'password': 'testpass',
        })
        self.assertEqual(response.status_code, 302)
        self.assertIn('headquarters_dashboard', response.url)
        # Session should be set
        self.assertEqual(self.client.session['isHeadquartersLoggedIn'], 1)

    def test_login_with_invalid_credentials(self):
        """POST with wrong credentials should show login page with error."""
        response = self.client.post('/user/login/', {
            'username': 'testlogin',
            'password': 'wrongpass',
        })
        self.assertEqual(response.status_code, 200)  # stays on login page
        self.assertIn('error', response.context)
        self.assertEqual(response.context['error'], 1)

    def test_login_redirects_if_already_logged_in(self):
        """If already logged in, GET /user/login should redirect to dashboard."""
        session = self.client.session
        session['isHeadquartersLoggedIn'] = 1
        session.save()
        response = self.client.get('/user/login/')
        self.assertEqual(response.status_code, 302)
        self.assertIn('headquarters_dashboard', response.url)
