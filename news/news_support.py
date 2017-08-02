'''
Script that grabs information from pravo.ru news website.
'''
# Author: Devin Johnson, University of Wisconsin - Madison, djohnson58@wisc.edu
# Bugs: None known

import requests
import datetime
from bs4 import BeautifulSoup

# Class that represents any given article from a website
class Article:
    title = ""
    description = ""
    date = ""
    link = ""
    source = ""

    def __init__(self, title, description, date, link, source):
        self.title = title
        self.description = description
        self.date = date
        self.link = link
        self.source = source

# A function to check if a date is today
def check_date(date, option):
    # Dictionary and variable for finding translation to scraped date
    dates_dict = {'января':'1', 'февраля':'2', 'марта':'3', 'апреля':'4', 'мая':'5', 'июня':'6', 'июля':'7',
    'августа':'8', 'сентября':'9', 'октября':'10', 'ноября':'11', 'декабря':'12'}
    advo_today = str(datetime.datetime.now().strftime("%d.%m.%Y"))

    if option == "pravo":
        return date[0] == str(datetime.datetime.now().day) and dates_dict.get(date[1]) == \
        str(datetime.datetime.now().month) and date[2].strip(',') == str(datetime.datetime.now().year)

    if option == "advo":
        return date == advo_today


# A function which constructs the body of the email to be sent
def construct_body(articles):
    body = ""
    for article in articles:
        if article.source == "pravo":
            body += article.title + "\n" + article.description + "\n" + article.date + "\n" + "https://pravo.ru" + \
            article.link + "\n\n"

        if article.source == "advo":
            body += article.title + "\n" + article.description + "\n" + article.date + "\n" + \
            "http://www.advgazeta.ru/" + article.link + "\n\n"

    return body

# A function using beautiful soup to collect page info
def collect_data(pages, option):
    # Populate soup objects
    soups = []
    for page in pages:
        soups.append(BeautifulSoup(requests.get(page).content, "html.parser"))

    # Arrays of page data
    titles = []
    descriptions = []
    dates = []
    links = []

    if option == "pravo":
        for soup in soups:
            # Populate titles
            for title in soup.select("h3 a"):
                titles.append(title.get_text(strip=True))

            # Populate descriptions
            for a in soup.select("p.news-text a"):
                descriptions.append(a.get_text(strip=True))

            # Populate dates
            for date in soup.select("div.info-bl div.date"):
                dates.append(date.get_text(strip=True))

            # Populate links
            for a in soup.select("h3 a"):
                links.append(a.get("href"))

    if option == "advo":
        # For each soup object
        for soup in soups:
            # Collect all dates, titles, descriptions in one array such that indices follow a pattern
            info = soup.select("p.news b")

            # Collect dates
            for i in range (0, len(info), 3):
                dates.append(info[i].get_text(strip=True))

            # Collect titles
            for i in range (1, len(info), 3):
                titles.append(info[i].get_text(strip=True))

            # Collect descriptions
            for i in range (2, len(info), 3):
                descriptions.append(info[i].get_text(strip=True))

            # Populate links
            for a in soup.select("p.news a"):
                links.append(a.get("href"))

    return titles, descriptions, dates, links


# A function which uses keywords to search for articles of interest from the main news pages of Pravo.ru
def keyword_scrape(words):
    keywords = words

    # Populate pages
    pravo_pages = []
    advo_pages = []
    for i in range (4):
        pravo_pages.append('https://pravo.ru/news/?page=' + str(i + 1))
        advo_pages.append('http://www.advgazeta.ru/newsd/nfrom' + str(i))

    # Make and collect info from soup objects
    pravo_titles, pravo_descriptions, pravo_dates, pravo_links = collect_data(pravo_pages, "pravo")
    advo_titles, advo_descriptions, advo_dates, advo_links = collect_data(advo_pages, "advo")

    # Create list to store article objects
    articles = []

    # Check pravo articles for keywords and current date
    for i in range (len(pravo_titles)):
        if any(keyword in pravo_titles[i].lower() or keyword in pravo_descriptions[i].lower() for keyword in keywords)\
        and check_date(pravo_dates[i].split(' '), "pravo"):
            articles.append(Article(pravo_titles[i], pravo_descriptions[i], pravo_dates[i], pravo_links[i], \
            "pravo"))

    # Check advo articles for keywords and current date
    for i in range (len(advo_titles)):
        if any(keyword in advo_titles[i].lower() or keyword in advo_descriptions[i].lower() for keyword in keywords)\
        and check_date(advo_dates[i], "advo"):
            articles.append(Article(advo_titles[i], advo_descriptions[i], advo_dates[i], advo_links[i], \
            "advo"))

    return construct_body(articles)


# A function which takes advantage of pre-filtered pages and grabs the daily article from each
def filter_scrape():
    # Dictionary to store tags and their respective page codes
    pravo_dict = {'Адвокатура': '682', 'Рынок юридических услуг': '2728', 'Интервью': '1923',
    'Юридическое сообщество': '6780', 'Нотариат': '1599', 'Генеральная прокуратура РФ': '247',
    'Следственный комитет РФ': '6827', 'Юридическая карьера': '6811', 'Юридический консалтинг': '7171',
    'Арбитражный процесс': '4328'}

    advo_dict = {'Адвокатура': '10', 'Юридическое орбазование': '23', 'Адвокатская кухня': '2',
    'Адвокатура, государство, общество': '24', 'Адвокатская этика': '5', 'Этика юриста': '44' }

    # Populate pages with urls
    pravo_pages = []
    advo_pages =[]
    for key in pravo_dict:
        pravo_pages.append('https://pravo.ru/tags/' + pravo_dict.get(key))
    for key in advo_dict:
        advo_pages.append('http://www.advgazeta.ru/blog/rubric/' + advo_dict.get(key))

    # Make and collect info from soup objects
    pravo_titles, pravo_descriptions, pravo_dates, pravo_links = collect_data(pravo_pages, 'pravo')
    advo_titles, advo_descriptions, advo_dates, advo_links = collect_data(advo_pages, 'advo')

    # Create list to store article objects
    articles = []

    # Check pravo articles for current date
    for i in range (len(pravo_titles)):
        if check_date(pravo_dates[i].split(' '), "pravo"):
            articles.append(Article(pravo_titles[i], pravo_descriptions[i], pravo_dates[i], pravo_links[i], \
            "pravo"))

    # Check advo articles for keywords and current date
    for i in range (len(advo_titles)):
        if check_date(advo_dates[i], "advo"):
            articles.append(Article(advo_titles[i], advo_descriptions[i], advo_dates[i], advo_links[i], \
            "advo"))

    return construct_body(articles)
