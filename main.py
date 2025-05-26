import os
import requests
import time
from datetime import datetime
from flask import Flask
from threading import Thread

# Flask app to keep bot alive
app = Flask('')

@app.route('/')
def home():
    return "Yellow Ops Bot is running!"

def run():
    app.run(host='0.0.0.0', port=8080)

def keep_alive():
    t = Thread(target=run)
    t.start()

# Get secrets from Replit
BOT_TOKEN = os.getenv("BOT_TOKEN")
CHAT_ID = os.getenv("CHAT_ID")

def send_message(text):
    url = f"https://api.telegram.org/bot{BOT_TOKEN}/sendMessage"
    data = {
        "chat_id": CHAT_ID,
        "text": text,
        "parse_mode": "Markdown"
    }
    try:
        requests.post(url, data=data)
    except Exception as e:
        print(f"Error sending message: {e}")

def loop():
    sent_today = {"10:00": False, "22:00": False}

    while True:
        now = datetime.now().strftime("%H:%M")

        # Reset flags at midnight
        if now == "00:01":
            sent_today = {"10:00": False, "22:00": False}

        if now == "10:00" and not sent_today["10:00"]:
            send_message("📈 *Morning Focus*\n- Review trade idea\n- Post update\n- Save ₹500\n- Make today count.")
            sent_today["10:00"] = True

        if now == "22:00" and not sent_today["22:00"]:
            send_message("🌙 *Evening Reflection*\n- Did you win today?\n- Log your result\n- Reset for tomorrow.")
            sent_today["22:00"] = True

        time.sleep(60)

# Start
keep_alive()
loop()
