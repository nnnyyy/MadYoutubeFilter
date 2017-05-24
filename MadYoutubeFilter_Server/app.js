var express = require('express');
var urlencode = require('urlencode');
var request = require('request');
var cheerio = require('cheerio')
var xml2js = require('xml2js');
var xmlParser = xml2js.Parser();
var app = express();
const dataAPIKey = '%2BldNua%2Fn0VEJt%2BtrwNRRz74Mvewgmu%2Fwz3P%2Fxtrc4GD%2BKO5Zx6oNgWYAaLpuTuBrWI6eCRMrgui%2BbdMVFvl4HQ%3D%3D';
const weatherFreeAPIKey = '42d9865a1ddccdaa8f5d2bd8494a3f6b'
const youtubeBrowerKey = 'AIzaSyCKfmVmbkI1-bFKVanEOwFTBDQr6sKZOuw'

app.get('/parkinginfo', function (req, res_parent) {

    var reqOptions = {
        url: 'http://openapi.airport.kr/openapi/service/StatusOfParking/getTrackingParking?serviceKey='+dataAPIKey+'&pageNo=1&startPage=1&numOfRows=10&pageSize=100',
        method: 'GET',
        headers: {
            'Accept' : 'application/xml',
            'Accept-Charset' : 'utf-8',
            'User-Agent' : 'my-reddit-client'
        }
    };

    try {
        request( reqOptions, function(err, res, body) {
            xmlParser.parseString(body, function(err, result) {
                res_parent.send(result.response.body[0].items[0].item);
            })
        });
    }
    catch(err) {
        console.log(err);
        res_parent.end(err);
    }
});

app.get('/terminalinfo/:no', function (req, res_parent) {

    var no = req.params.no;
    var reqOptions = {
        url: 'http://openapi.airport.kr/openapi/service/StatusOfDepartures/getDeparturesCongestion?serviceKey='+dataAPIKey+'&terno=' + no,
        method: 'GET',
        headers: {
            'Accept' : 'application/xml',
            'Accept-Charset' : 'utf-8',
            'User-Agent' : 'my-reddit-client'
        }
    };

    try {
        request( reqOptions, function(err, res, body) {
            xmlParser.parseString(body, function(err, result) {
                res_parent.send(result.response.body[0].items[0].item);
            })
        });
    }
    catch(err) {
        console.log(err);
        res_parent.end(err);
    }
});

app.get('/list' , function(req,res_parent) {
    res_parent.send([{name:"인기", key:"인기"},{name:"라이브", key:"live"}]);
})


app.get('/search/:arg1' , function(req,res_parent) {
    var url_final = 'https://www.googleapis.com/youtube/v3/search?part=snippet&key='+youtubeBrowerKey+'&maxResults=20&type=video&q='+urlencode(req.params.arg1);
    if(req.query.pageToken != null) {
        url_final += '&pageToken=' + req.query.pageToken;
    }
    var reqOptions = {
        url: url_final,
        method: 'GET',
        headers: {
            'Accept' : 'application/xml',
            'Accept-Charset' : 'utf-8',
            'User-Agent' : 'my-reddit-client'
        }
    };

    try {
        request( reqOptions, function(err, res, body) {
            var list = []
            var nextPageToken = JSON.parse(body).nextPageToken;
            var prevPageToken = JSON.parse(body).prevPageToken;
            for( var i = 0 ; i < JSON.parse(body).items.length ; ++i) {
                var title = JSON.parse(body).items[i].snippet.title;
                var thumnails = JSON.parse(body).items[i].snippet.thumbnails.default.url;
                var chtitle = JSON.parse(body).items[i].snippet.channelTitle;
                var id = JSON.parse(body).items[i].id.videoId;
                list.push({id:id, title:title, thumnails:thumnails, chtitle:chtitle});
            }
            res_parent.send({prevToken:prevPageToken, nextToken:nextPageToken, contents:list});
        });
    }
    catch(err) {
        console.log(err);
        res_parent.end(err);
    }
})

app.get('/videosinfo/:arg1' , function(req,res_parent) {
    var reqOptions = {
        url: 'https://www.googleapis.com/youtube/v3/videos?part=contentDetails&key='+youtubeBrowerKey+'&id='+req.params.arg1,
        method: 'GET',
        headers: {
            'Accept' : 'application/xml',
            'Accept-Charset' : 'utf-8',
            'User-Agent' : 'my-reddit-client'
        }
    };

    try {
        request( reqOptions, function(err, res, body) {
            var list = []
            for( var i = 0 ; i < JSON.parse(body).items.length ; ++i) {
                var item = JSON.parse(body).items[i];
                var id = item.id;
                var duration = item.contentDetails.duration;
                var definition = item.contentDetails.definition;
                list.push({id:id, duration: duration, definition:definition});
            }
            res_parent.send({contents:list});
        });
    }
    catch(err) {
        console.log(err);
        res_parent.end(err);
    }
})

app.listen(4000, function() {
    console.log('Today\'s Video listening on port 4000!');
})