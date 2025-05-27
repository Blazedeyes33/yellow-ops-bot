import os
import requests
import time
import pytz
from datetime import datetime
from flask import Flask
from threading import Thread

# --- Flask app to keep Railway container alive ---
app = Flask('')

@app.route('/')
def home():
    return "Yellow Ops Bot is running (IST Timezone)"

def run():
    app.run(host='0.0.0.0', port=8080)

def keep_alive():
    t = Thread(target=run)
    t.start()

# --- Telegram Bot Setup ---
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
        response = requests.post(url, data=data)
        print("Sent:", response.text)  # Log Telegram's API response
    except Exception as e:
        print("Error sending message:", e)

# --- Ping Logic with IST Timezone ---
def loop():
    sent_today = {"10:00": False, "22:00": False}
    ist = pytz.timezone('Asia/Kolkata')

    while True:
        now = datetime.now(ist).strftime("%H:%M")

        # Reset flags at 00:01
        if now == "00:01":
            sent_today = {"10:00": False, "22:00": False}

        if now == "10:00" and not sent_today["10:00"]:
            send_message("📈 *Morning Focus*\n- Review 1 trade idea\n- Post content (YT/TG)\n- Save ₹500\n- Progress = Freedom")
            sent_today["10:00"] = True

        if now == "22:00" and not sent_today["22:00"]:
            send_message("🌙 *Evening Reflection*\nDid you win today? Yes / No\n- Log your result\n- Reset for tomorrow.")
            sent_today["22:00"] = True

        time.sleep(60)

# --- Start Everything ---
keep_alive()
loop()

