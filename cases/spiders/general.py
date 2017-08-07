import scrapy

url = input("Enter the starting page of results: ")
page_count = input("Enter the amount of pages to scrape: ")
links = []

class GeneralSpider(scrapy.Spider):
    name = "general"
    start_urls = []

    # Populate start_urls
    for i in range (int(page_count)):
        start_urls.append(url + "page-" + str(i))

    def parse(self, response):
        for link in response.css('.avh::attr(href)'):
            links.append('https://rospravosudie.com/' + link.extract())
