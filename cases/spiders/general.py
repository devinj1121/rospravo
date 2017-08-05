import scrapy

url = input("Enter the starting page of results: ")
page_count = input("Enter the amount of pages to scrape: ")

class BatchSpider(scrapy.Spider):
    name = "general"
    start_urls = []

    # Populate start_urls
    for i in range page_count:
        start_urls.append(url + "page-" + str(i))

    def parse(self, response):

        next_page = response.css('li.next a::attr(href)').extract_first()
        if next_page is not None:
            yield response.follow(next_page, self.parse)
