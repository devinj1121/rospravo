'''
Script that provides uses adv.scrape and pravo_scrape to scrape information from both news websites.
Acts as main file for whole project.
'''
# Author: Devin Johnson, University of Wisconsin - Madison, djohnson58@wisc.edu
# Bugs: None known

import smtplib
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
import news_support

# Email information
fr = input("Please enter your gmail address: ")
pw = input("Please enter your gmail password: ")
to = input("Please enter the recipient's address: ")

msg = MIMEMultipart()
msg['From'] = fr
msg['To'] = to
msg['Subject'] = "News scrape"

try:
    server = smtplib.SMTP('smtp.gmail.com', 587)
    server.starttls()
    server.login(fr, pw)
except smtplib.SMTPAuthenticationError:
    print("Error in authenticating email information, please try again.")
    quit()


# Select which kind of scrape to use
body = ""
keywords = ['эксперт', 'юрист', 'адвокат', 'интервью', 'мнение', 'долж', 'рассказыв', 'эксперимен', 'опрос',
            'треть', 'четверть', 'половина', 'думает', 'думают', 'считает', 'считают', 'бизнес']

print("\n" + "Keywords: " + str(keywords) + "\n")
choice = input("Enter 1 to search based on given keywords, enter 2 to search based upon website tags: ")
while True:
    if choice == "1":
        print("Working...")
        body += news_support.keyword_scrape(keywords)
        break
    elif choice == "2":
        print("Working...")
        body += news_support.filter_scrape()
        break
    else:
        choice = input("Please enter a correct value. Enter 1 to search based on given keywords, enter 2 to " + \
        "search based upon website tags: ")

# If the body is empty, don't send the email
if not body:
    print("No articles found, no email sent.")
else:
    print(body)
    print("Some articles found, email sent.")
    msg.attach(MIMEText(body,'plain'))
    server.sendmail(fr, to, msg.as_string())
