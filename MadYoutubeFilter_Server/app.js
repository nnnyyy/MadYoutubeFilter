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
    res_parent.send([
        {name:"Hot", key:"mostPopular", type:"chart"},
        {name:"BJ", key:"BJ", type:"search"},
        {name:"Music", key:"", type:"playlist", subCategory:[{name:"Hot", key:"PLTDluH66q5mpm-Bsq3GlwjMOHITt2bwXE"},{name:"New", key:"PLTDluH66q5mq_h0fwkBFtMSRY7sPgcovp"}]}
    ]);
})


var YoutubeSearch = function(params, pageToken, handler) {
    var url_final = 'https://www.googleapis.com/youtube/v3/search?part=snippet&key='+youtubeBrowerKey+'&maxResults=20&type=video&q='+urlencode(params);
    if(pageToken != null) {
        url_final += '&pageToken=' + pageToken;
    }
    var reqOptions = {
        url: url_final,
        method: 'GET',
        headers: {
            'Accept' : 'application/xml',
            'Accept-Charset' : 'utf-8',
            'User-Agent' : 'my-reddit-client'
        }
    }

    try {
        request( reqOptions, function(err, res, body) {
            var sRet = "";
            for( var i = 0 ; i < JSON.parse(body).items.length ; ++i) {
                var id = JSON.parse(body).items[i].id.videoId;
                sRet += (id + ",");
            }
            handler(JSON.stringify(sRet));
        });
    }
    catch(err) {
        console.log(err);
        handler("");
    }
}

var YoutubePlaylist = function(params, pageToken, handler) {
    var url_final = 'https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&key='+youtubeBrowerKey+'&maxResults=20&playlistId='+params;
    if(pageToken != null) {
        url_final += '&pageToken=' + pageToken;
    }
    var reqOptions = {
        url: url_final,
        method: 'GET',
        headers: {
            'Accept' : 'application/xml',
            'Accept-Charset' : 'utf-8',
            'User-Agent' : 'my-reddit-client'
        }
    }

    try {
        request( reqOptions, function(err, res, body) {
            var sRet = "";
            for( var i = 0 ; i < JSON.parse(body).items.length ; ++i) {
                var id = JSON.parse(body).items[i].snippet.resourceId.videoId;
                sRet += (id + ",");
            }
            handler(JSON.stringify(sRet));
        });
    }
    catch(err) {
        console.log(err);
        handler("");
    }
}

var YoutubeGetVideos = function(url, res_parent) {
    var reqOptions = {
        url: url,
        method: 'GET',
        headers: {
            'Accept' : 'application/xml',
            'Accept-Charset' : 'utf-8',
            'User-Agent' : 'my-reddit-client'
        }
    };

    console.log(url);

    try {
        request( reqOptions, function(err, res, body) {
            var list = []
            var jsonRoot = JSON.parse(body);
            var nextPageToken = jsonRoot.nextPageToken;
            var prevPageToken = jsonRoot.prevPageToken;
            for( var i = 0 ; i < jsonRoot.items.length ; ++i) {
                var item = jsonRoot.items[i];
                var title = item.snippet.title;
                var thumnails = item.snippet.thumbnails.medium.url;
                var chtitle = item.snippet.channelTitle;
                var id = item.id;
                var duration = item.contentDetails.duration;
                var definition = item.contentDetails.definition;
                var viewCnt = item.statistics.viewCount;
                var commentCnt = item.statistics.commentCount;
                list.push({id:id, title:title, thumnails:thumnails, chtitle:chtitle, duration: duration, definition:definition, viewCnt: viewCnt, commentCnt: commentCnt});
            }
            console.log({prevToken:prevPageToken, nextToken:nextPageToken, contents:list});
            res_parent.send({prevToken:prevPageToken, nextToken:nextPageToken, contents:list});
        });
    }
    catch(err) {
        console.log(err);
        res_parent.end(err);
    }
}
// 최신 트렌드
// https://www.googleapis.com/youtube/v3/videos?part=snippet,contentDetails&chart=mostPopular&regionCode=KR&maxResults=12&key=AIzaSyCKfmVmbkI1-bFKVanEOwFTBDQr6sKZOuw
app.get('/v/:arg1' , function(req,res_parent) {
    var url_final = 'https://www.googleapis.com/youtube/v3/videos?part=snippet,contentDetails,statistics&regionCode=KR&maxResults=12&key='+youtubeBrowerKey;
    if(req.query.pageToken != null) {
        url_final += '&pageToken=' + req.query.pageToken;
    }
    switch(req.query.contentType) {
        case "chart":
            url_final += "&chart="+urlencode(req.params.arg1);
            YoutubeGetVideos(url_final, res_parent);
            break;

        case "search":
            YoutubeSearch(req.params.arg1, req.query.pageToken, function(ret) {
                url_final += ('&id=' + ret);
                YoutubeGetVideos(url_final, res_parent);
            });
            break;

        case "playlist":
            YoutubePlaylist(req.params.arg1, req.query.pageToken, function(ret){
                url_final += ('&id=' + ret);
                YoutubeGetVideos(url_final, res_parent);
            });
            break;

        default:
            break;
    }

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
        url: 'https://www.googleapis.com/youtube/v3/videos?part=contentDetails,statistics&key='+youtubeBrowerKey+'&id='+req.params.arg1,
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
                var viewCnt = item.statistics.viewCount;
                var commentCnt = item.statistics.commentCount;
                list.push({id:id, duration: duration, definition:definition, viewCnt: viewCnt, commentCnt: commentCnt});
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