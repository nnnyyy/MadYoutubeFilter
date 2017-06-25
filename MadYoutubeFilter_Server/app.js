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

app.get('/list' , function(req,res_parent) {

    var list = [
        {name:"Hot", key:"mostPopular", type:"chart"},
        {name:"BJ", key:"BJ", type:"search"},
        {name:"Music", key:"", type:"", subCategory:[
            {name:"Hot", key:"PLTDluH66q5mpm-Bsq3GlwjMOHITt2bwXE", type:"playlist"},
            {name:"New", key:"PLTDluH66q5mq_h0fwkBFtMSRY7sPgcovp", type:"playlist"},
            {name:"POP", key:"PLDcnymzs18LWrKzHmzrGH1JzLBqrHi3xQ", type:"playlist"}
        ]},
        {name:"Live", key:"PLU12uITxBEPGpEPrYAxJvNDP6Ugx2jmUx", type:"playlist"}
    ];

    switch(req.query.regionCode) {
        case "JP":
            list = [
                {name:"Hot", key:"mostPopular", type:"chart"},
                {name:"BJ", key:"BJ", type:"search"},
                {name:"Live", key:"PLU12uITxBEPGpEPrYAxJvNDP6Ugx2jmUx", type:"playlist"}
            ]
            break;
    }

    res_parent.send(list);
})


var YoutubeSearch = function(query, params, pageToken, handler) {
    var url_final = 'https://www.googleapis.com/youtube/v3/search?part=snippet&key='+youtubeBrowerKey+'&maxResults=20&type=video&q='+urlencode(params);
    if(pageToken != null) {
        url_final += '&pageToken=' + pageToken;
    }
    url_final += '&regionCode=' + query.regionCode;
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
            try {
                var sRet = "";
                var nextToken = JSON.parse(body).nextPageToken;
                var prevToken = JSON.parse(body).prevPageToken;
                if(JSON.parse(body).items != null) {
                    for( var i = 0 ; i < JSON.parse(body).items.length ; ++i) {
                        var id = JSON.parse(body).items[i].id.videoId;
                        sRet += (id + ",");
                    }
                    handler({sRet: JSON.stringify(sRet), nextToken:nextToken, prevToken:prevToken});
                }
                else {
                    handler({sRet:""});
                }
            }
            catch(e) {
                handler({sRet:""});
                console.log("YoutubeSearch: " + e + " , " + url_final);
            }
        });
    }
    catch(err) {
        console.log(err);
        handler({sRet:""});
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
            try {
                var sRet = "";
                var nextToken = JSON.parse(body).nextPageToken;
                var prevToken = JSON.parse(body).prevPageToken;
                if(JSON.parse(body).items != null) {
                    for( var i = 0 ; i < JSON.parse(body).items.length ; ++i) {
                        var id = JSON.parse(body).items[i].snippet.resourceId.videoId;
                        sRet += (id + ",");
                    }
                    handler({sRet: JSON.stringify(sRet), nextToken:nextToken, prevToken:prevToken});
                }
                else {
                    handler({sRet:""});
                }
            }
            catch(e) {
                handler({sRet:""});
                console.log("YoutubePlaylist: " + e + " , " + url_final);
            }
        });
    }
    catch(err) {
        console.log(err);
        handler({sRet:""});
    }
}

var YoutubeGetVideos = function(url, nextToken, prevToken, res_parent) {
    var reqOptions = {
        url: url,
        method: 'GET',
        headers: {
            'Accept' : 'application/xml',
            'Accept-Charset' : 'utf-8',
            'User-Agent' : 'my-reddit-client'
        }
    };

    //console.log(url);

    try {
        request( reqOptions, function(err, res, body) {
            try {
                var list = []
                var jsonRoot = JSON.parse(body);
                var nextPageToken = nextToken;
                if( JSON.parse(body).nextPageToken ) {
                    nextPageToken = JSON.parse(body).nextPageToken;
                }
                var prevPageToken = prevToken;
                if( JSON.parse(body).prevPageToken ) {
                    prevPageToken = JSON.parse(body).prevPageToken;
                }
                if(jsonRoot.items != null) {
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
                    //console.log({prevToken:prevPageToken, nextToken:nextPageToken, contents:list});
                    res_parent.send({ret:0,prevToken:prevPageToken, nextToken:nextPageToken, contents:list});
                }
                else {
                    res_parent.end({ret:-1, prevToken:"", nextToken:"", contents:list});
                }
            }
            catch(e) {
                res_parent.end({ret:-1, prevToken:"", nextToken:""});
                console.log("YoutubeGetVideos: " + e + ", " + url);
            }
        });
    }
    catch(err) {
        console.log(err);
        res_parent.end(err);
    }
}

app.get('/fav/:arg1', function(req, res_parent) {
    var url_final = 'https://www.googleapis.com/youtube/v3/videos?part=snippet,contentDetails,statistics&maxResults=20&key='+youtubeBrowerKey;
    url_final += ('&id=' + req.params.arg1);
    if(req.query.pageToken != null) {
        url_final += '&pageToken=' + req.query.pageToken;
    }
    YoutubeGetVideos(url_final, "", "", res_parent);
})

// 최신 트렌드
// https://www.googleapis.com/youtube/v3/videos?part=snippet,contentDetails&chart=mostPopular&regionCode=KR&maxResults=12&key=AIzaSyCKfmVmbkI1-bFKVanEOwFTBDQr6sKZOuw
app.get('/v/:arg1' , function(req,res_parent) {
    var regionCode = req.query.regionCode;
    var url_final = 'https://www.googleapis.com/youtube/v3/videos?part=snippet,contentDetails,statistics&maxResults=20&key='+youtubeBrowerKey;
    switch(req.query.contentType) {
        case "chart":
            if(req.query.pageToken != null) {
                url_final += '&pageToken=' + req.query.pageToken;
            }
            url_final += '&regionCode=' + regionCode;
            url_final += "&chart="+urlencode(req.params.arg1);
            YoutubeGetVideos(url_final, "", "", res_parent);
            break;

        case "search":
            YoutubeSearch(req.query, req.params.arg1, req.query.pageToken, function(ret) {
                url_final += ('&id=' + JSON.parse(ret.sRet));
                YoutubeGetVideos(url_final, ret.nextToken, ret.prevToken, res_parent);
            });
            break;

        case "playlist":
            YoutubePlaylist(req.params.arg1, req.query.pageToken, function(ret){
                url_final += ('&id=' + JSON.parse(ret.sRet));
                YoutubeGetVideos(url_final, ret.nextToken, ret.prevToken, res_parent);
            });
            break;

        default:
            break;
    }

})



app.listen(4000, function() {
    console.log('Today\'s Video listening on port 4000!');
})