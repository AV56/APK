# Appa Call — How to get the app onto your dad's phone

You do **not** need to know coding, and you do **not** need to install any
programs on your computer. The app is built for you automatically on a free
website (GitHub). You download the finished file and copy it to the phone.

Total time: about 20–30 minutes the first time. If you have a friend or
relative who is comfortable with computers, this is simple enough to follow in
one sitting.

---

## What you are doing, in plain words

1. Put this project on a free website called GitHub.
2. GitHub builds the installable app file for you (called an **APK**).
3. You download that file and copy it to your dad's phone.
4. You tap the file on the phone to install the app.

---

## Step 1 — Make a free GitHub account
1. Go to https://github.com in any web browser.
2. Click **Sign up** and create a free account (email + password).

## Step 2 — Create a place for the project
1. After signing in, click the **+** at the top-right, then **New repository**.
2. For **Repository name**, type: `appa-call`
3. Leave everything else as it is. Click **Create repository**.

## Step 3 — Upload the project files
1. On the new repository page, click the link **"uploading an existing file"**.
2. On your computer, open the `AppaCall` folder.
3. Select **everything inside** it and drag it into the GitHub upload box.
   Important: include the hidden `.github` folder — that is the part that builds
   the app. (If dragging the hidden folder is tricky, use the free **GitHub
   Desktop** app, or ask a tech-comfortable person to do just this one step.)
4. Scroll down and click the green **Commit changes** button.

## Step 4 — Let GitHub build the app
1. Click the **Actions** tab near the top of the repository page.
2. Look for **"Build Appa Call APK"** running (a yellow dot). Wait until it
   becomes a **green tick** (about 3–6 minutes). If it is not running, open the
   workflow on the left and click **Run workflow**.

## Step 5 — Download the app file (APK)
1. Click the finished green-tick build.
2. Scroll to the **Artifacts** section at the bottom.
3. Click **AppaCall-app** to download it (a `.zip`).
4. Unzip it. Inside is **`app-debug.apk`** — that is the app.

## Step 6 — Install on your dad's phone
1. Send `app-debug.apk` to the phone (email, Google Drive, WhatsApp, or USB).
2. On the phone, tap the `app-debug.apk` file.
3. Android asks to **allow installing unknown apps** — tap **Settings**, turn
   the switch **ON**, go back, and tap **Install**.
4. Open the app. The first time it asks permission for **Contacts** and to
   **make phone calls** — tap **Allow** for both.

Done — the app is installed.

---

## First-time setup inside the app (do this once for your dad)
- Tap **Settings** (top right) and choose the colours that are easiest for him
  (Yellow on Black is the default and usually best for RP).
- Use **Bigger / Smaller** to set the text size.
- Tap **Choose SOS Contact** and pick yourself (or whoever should be the
  emergency call). The big red **SOS** button at the bottom will then call them.
- Back on the main screen, tap the **New Group** circle to make groups like
  "Family", "Doctors", "Friends", then add the right people to each.

## If the build shows a red X (failed)
This occasionally happens on the first try because online tools update often.
Open the failed build, click the **Build debug APK** step, copy the message,
and send it to me — I'll give you the one-line fix.

## Updating later
Re-upload changed files to GitHub; it rebuilds the APK automatically. Install
the new APK over the old one — his groups and settings are kept.

## Fastest route for a developer friend
Open the `AppaCall` folder in **Android Studio**, let it sync, then **Run** on a
connected phone or **Build > Build APK(s)**. Kotlin, min Android 8.0 (API 26),
no external services.
