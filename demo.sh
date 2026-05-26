#!/bin/bash
# CodeRescue Demo - Start/Stop Script
# Usage: ./demo.sh start | stop | seed | status

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
DJANGO_DIR="$PROJECT_DIR/web/disaster_management"
FLASK_DIR="$PROJECT_DIR/web/sosEndpointApi"
VENV="$DJANGO_DIR/venv/bin/activate"

case "$1" in
  start)
    echo "=== Starting CodeRescue Demo ==="

    # 1. Start MongoDB (Docker)
    if docker ps --format '{{.Names}}' | grep -q coderescue-mongo; then
      echo "[OK] MongoDB already running"
    else
      echo "[..] Starting MongoDB..."
      docker start coderescue-mongo 2>/dev/null || docker run -d --name coderescue-mongo -p 27017:27017 mongo:7
      echo "[OK] MongoDB started on port 27017"
    fi

    # 2. Start Django
    if lsof -i :8000 -sTCP:LISTEN > /dev/null 2>&1; then
      echo "[OK] Django already running on port 8000"
    else
      echo "[..] Starting Django..."
      cd "$DJANGO_DIR" && source "$VENV" && python manage.py runserver 8000 > /tmp/django_server.log 2>&1 &
      sleep 2
      echo "[OK] Django started on http://localhost:8000/main/"
    fi

    # 3. Start Flask SOS API
    if lsof -i :5001 -sTCP:LISTEN > /dev/null 2>&1; then
      echo "[OK] Flask SOS API already running on port 5001"
    else
      echo "[..] Starting Flask SOS API..."
      cd "$FLASK_DIR" && source "$VENV" && python index.py > /tmp/flask_server.log 2>&1 &
      sleep 2
      echo "[OK] Flask SOS API started on http://localhost:5001/"
    fi

    echo ""
    echo "=== All services running ==="
    echo "  Web Dashboard:  http://localhost:8000/main/"
    echo "  HQ Dashboard:   http://localhost:8000/main/headquarters_dashboard"
    echo "  HQ Login:       admin / admin123"
    echo "  Flask SOS API:  http://localhost:5001/"
    echo "  MongoDB:        localhost:27017"
    echo ""
    echo "  Rescue Team Logins (for Android app):"
    echo "    rescue_kerala_1 / rescue123"
    echo "    rescue_uk_1 / rescue123"
    echo "    rescue_wb_1 / rescue123"
    ;;

  stop)
    echo "=== Stopping CodeRescue Demo ==="
    # Kill Django
    kill $(lsof -ti :8000 -sTCP:LISTEN) 2>/dev/null && echo "[OK] Django stopped" || echo "[--] Django not running"
    # Kill Flask
    kill $(lsof -ti :5001 -sTCP:LISTEN) 2>/dev/null && echo "[OK] Flask stopped" || echo "[--] Flask not running"
    # Stop MongoDB
    docker stop coderescue-mongo 2>/dev/null && echo "[OK] MongoDB stopped" || echo "[--] MongoDB not running"
    echo "=== All services stopped ==="
    ;;

  seed)
    echo "=== Seeding demo data ==="
    cd "$DJANGO_DIR" && source "$VENV" && python "$PROJECT_DIR/seed_demo_data.py"
    ;;

  status)
    echo "=== CodeRescue Service Status ==="
    lsof -i :27017 -sTCP:LISTEN > /dev/null 2>&1 && echo "[OK] MongoDB on port 27017" || echo "[--] MongoDB NOT running"
    lsof -i :8000 -sTCP:LISTEN > /dev/null 2>&1 && echo "[OK] Django on port 8000" || echo "[--] Django NOT running"
    lsof -i :5001 -sTCP:LISTEN > /dev/null 2>&1 && echo "[OK] Flask on port 5001" || echo "[--] Flask NOT running"
    ;;

  *)
    echo "Usage: $0 {start|stop|seed|status}"
    exit 1
    ;;
esac
