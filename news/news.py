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
import os

# Email information
fr = os.environ['USER']
pw = os.environ['PW']
to = os.environ['RECIP']

msg = MIMEMultipart()
msg['From'] = fr
msg['To'] = to
msg['Subject'] = "News scrape"

try:
    server = smtplib.SMTP('smtp.sendgrid.net', 587)
    server.starttls()
    server.login(fr, pw)
except smtplib.SMTPAuthenticationError as e:
    print("Error in authenticating email information, please try again: " + str(e))
    quit()

# Select which kind of scrape to use
body = ""
keywords = ['эксперт', 'юрист', 'адвокат', 'интервью', 'мнение', 'долж', 'рассказыв', 'эксперимен', 'опрос',
            'треть', 'четверть', 'половина', 'думает', 'думают', 'считает', 'считают', 'бизнес']
choice = 2

print("Working...")
if choice == 1:
    body += news_support.keyword_scrape(keywords)
elif choice == 2:
    body += news_support.filter_scrape()

# If the body is empty, don't send the email
if not body:
    print("No articles found, no email sent.")
else:
    print("Some articles found, email sent.")
    msg.attach(MIMEText(body,'plain'))
    server.sendmail(fr, to, msg.as_string())
