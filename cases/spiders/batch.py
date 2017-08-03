# -*- coding: utf-8 -*-
import scrapy


class BatchSpider(scrapy.Spider):
    name = 'batch'
    allowed_domains = ['www.rospravosudie.com']
    start_urls = ['http://www.rospravosudie.com/']

    def parse(self, response):
        page = response.url.split("/")[-2]
        filename = 'rospravo-%s.html' % page
        with open(filename, 'wb') as f:
            f.write(response.body)
        self.log('Saved file %s' % filename)
