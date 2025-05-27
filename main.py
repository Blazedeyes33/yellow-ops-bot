import os
import requests
import time
import pytz
from datetime import datetime
from flask import Flask
from threading import Thread

# --- Notion API Setup ---
NOTION_TOKEN = os.getenv("NOTION_TOKEN")
NOTION_DB_ID = os.getenv("NOTION_DB_ID")
NOTION_HEADERS = {
    "Authorization": f"Bearer {NOTION_TOKEN}",
    "Notion-Version": "2022-06-28",
    "Content-Type": "application/json"
}

# --- Flask Keep-Alive ---
app = Flask('')

@app.route('/')
def home():
    return "Yellow Ops Notion Bot is running (IST Time)"

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
        print("Telegram response:", response.text)
    except Exception as e:
        print("Error sending Telegram message:", e)

# --- Notion Task Fetcher ---
def fetch_notion_tasks():
    url = f"https://api.notion.com/v1/databases/{NOTION_DB_ID}/query"

    # Get today's date in YYYY-MM-DD format
    ist = pytz.timezone('Asia/Kolkata')
    today = datetime.now(ist).date().isoformat()

    query = {
        "filter": {
            "property": "Due Date",
            "date": {
                "equals": today
            }
        }
    }

    response = requests.post(url, headers=NOTION_HEADERS, json=query)

    if response.status_code != 200:
        print("Failed to fetch Notion tasks:", response.text)
        return "⚠️ Couldn't fetch tasks from Notion."

    data = response.json()
    results = data.get("results", [])
    if not results:
        return "✅ No tasks scheduled for today. You're clear!"

    # Parse the first row (today's tasks)
    props = results[0]["properties"]
    trade = props.get("Trade", {}).get("rich_text", [{}])[0].get("text", {}).get("content", "")
    content = props.get("Content", {}).get("rich_text", [{}])[0].get("text", {}).get("content", "")
    save = props.get("Save", {}).get("rich_text", [{}])[0].get("text", {}).get("content", "")
    repay = props.get("Debt Repay", {}).get("rich_text", [{}])[0].get("text", {}).get("content", "")
    review = props.get("Review", {}).get("rich_text", [{}])[0].get("text", {}).get("content", "")

    return f"""📈 *Morning Focus — {today}*
- Trade: {trade}
- Content: {content}
- Save: {save}
- Debt Repay: {repay}
- Review: {review}
"""

# --- Bot Logic with IST ---
def loop():
    sent_today = {"10:00": False, "22:00": False}
    ist = pytz.timezone('Asia/Kolkata')

    while True:
        now = datetime.now(ist).strftime("%H:%M")

        if now == "00:01":
            sent_today = {"10:00": False, "22:00": False}

        if not sent_today["10:00"]:
            notion_tasks = fetch_notion_tasks()
            send_message(notion_tasks)
            sent_today["10:00"] = True

        if now == "22:00" and not sent_today["22:00"]:
            send_message("🌙 *Evening Reflection*\nDid you win today? Yes / No\n- Log your result\n- Reset for tomorrow.")
            sent_today["22:00"] = True

        time.sleep(60)

# --- Start Bot ---
keep_alive()
loop()


