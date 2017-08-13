import scrapy

url = input("Enter the starting page of results: ")
page_count = input("Enter the amount of pages to scrape: ")

class GeneralSpider(scrapy.Spider):
    name = "general"
    allowed_domains = ["rospravosudie.com"]
    BASE_URL = "https://rospravosudie.com/"
    start_urls = []

    # Populate start_urls
    for i in range (int(page_count)):
        start_urls.append(url + "page-" + str(i))

    def parse(self, response):
        for link in response.css('.avh::attr(href)').extract():
            absolute_url = self.BASE_URL + link
            yield scrapy.Request(absolute_url, callback=self.parse_page)

    def parse_page(self, response):
        string = ''.join(response.css('p::text').extract())
        
        yield {
                'title': response.css('div h1::text').extract(),
                'type': response.css('th::text')[0].extract(),
                'stage': response.css('th::text')[1].extract(),
                'url': response.url,
                'text': string,
            }
