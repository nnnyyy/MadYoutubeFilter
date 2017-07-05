var express = require('express');
var urlencode = require('urlencode');
var request = require('request');
var cheerio = require('cheerio')
var xml2js = require('xml2js');
var bodyParser = require('body-parser');
var xmlParser = xml2js.Parser();
var app = express();
const dataAPIKey = '%2BldNua%2Fn0VEJt%2BtrwNRRz74Mvewgmu%2Fwz3P%2Fxtrc4GD%2BKO5Zx6oNgWYAaLpuTuBrWI6eCRMrgui%2BbdMVFvl4HQ%3D%3D';
const weatherFreeAPIKey = '42d9865a1ddccdaa8f5d2bd8494a3f6b'
const youtubeBrowerKey = 'AIzaSyCKfmVmbkI1-bFKVanEOwFTBDQr6sKZOuw'
var list = []

app.use(bodyParser.urlencoded({extended:true}))

app.post('/list_update', function(req,res){
    try {
        var jsontext = req.param('listdata', null);
        //var jsontext = '[{"name":"Hot", "key":"mostPopular", "type":"chart"}]';
        var contact = JSON.parse(jsontext);
        list = contact;
        res.send(contact);
    }
    catch(err) {
        res.end('wrong json format - update failed : ' + jsontext);
    }
})

app.get('/list' , function(req,res_parent) {

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
    if(query.regionCode != null) {
        url_final += '&regionCode=' + query.regionCode;
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
                        var thumnails = item.snippet.thumbnails.default.url;
                        if( item.snippet.thumbnails.high != null ) {
                            thumbnails = item.snippet.thumbnails.high.url;
                        }
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
                    res_parent.send({ret:-1, prevToken:"", nextToken:"", contents:list});
                }
            }
            catch(e) {
                res_parent.send({ret:-1, prevToken:"", nextToken:""});
                console.log("YoutubeGetVideos: " + e + ", " + url);
            }
        });
    }
    catch(err) {
        console.log(err);
        res_parent.end(err);
    }
}

app.get('/comments/:arg1', function(req, res_parent) {
    var url_final = 'https://www.googleapis.com/youtube/v3/commentThreads?part=replies,snippet&key='+youtubeBrowerKey;
    url_final += ('&videoId=' + req.params.arg1);
    if(req.query.isRelevance != null) {
        url_final += ('&order=relevance');
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
            try {
                var list = []
                var jsonRoot = JSON.parse(body);
                var nextPageToken = "";
                if( JSON.parse(body).nextPageToken ) {
                    nextPageToken = JSON.parse(body).nextPageToken;
                }
                var prevPageToken = "";
                if( JSON.parse(body).prevPageToken ) {
                    prevPageToken = JSON.parse(body).prevPageToken;
                }
                if(jsonRoot.items != null) {
                    for( var i = 0 ; i < jsonRoot.items.length ; ++i) {
                        var item = jsonRoot.items[i];
                        var comment = item.snippet.topLevelComment.snippet.textOriginal;
                        var authorName = item.snippet.topLevelComment.snippet.authorDisplayName;
                        var likecnt = item.snippet.topLevelComment.snippet.likeCount;
                        var publish_date = item.snippet.topLevelComment.snippet.publishedAt;
                        var update_date = item.snippet.topLevelComment.snippet.updatedAt;
                        /*
                         var title = item.snippet.title;
                         var thumnails = item.snippet.thumbnails.medium.url;
                         var chtitle = item.snippet.channelTitle;
                         var id = item.id;
                         var duration = item.contentDetails.duration;
                         var definition = item.contentDetails.definition;
                         var viewCnt = item.statistics.viewCount;
                         var commentCnt = item.statistics.commentCount;
                         list.push({id:id, title:title, thumnails:thumnails, chtitle:chtitle, duration: duration, definition:definition, viewCnt: viewCnt, commentCnt: commentCnt});
                         */
                        list.push({comment:comment, authname:authorName, likecnt:likecnt, pdate:publish_date, udate:update_date});
                    }
                    //console.log({prevToken:prevPageToken, nextToken:nextPageToken, contents:list});
                    res_parent.send({ret:0,prevToken:prevPageToken, nextToken:nextPageToken, contents:list});
                }
                else {
                    res_parent.send({ret:-1, prevToken:"", nextToken:"", contents:list});
                }
            }
            catch(e) {
                res_parent.send({ret:-1, prevToken:"", nextToken:""});
                console.log("YoutubeGetVideos: " + e + ", " + url_final);
            }
        });
    }
    catch(err) {
        console.log(err);
        res_parent.end(err);
    }
})

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
                try {
                    url_final += ('&id=' + JSON.parse(ret.sRet));
                    YoutubeGetVideos(url_final, ret.nextToken, ret.prevToken, res_parent);
                }catch(err){
                    res_parent.send({ret:-1, prevToken:"", nextToken:"", data: ret.sRet})
                }
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

app.get('/admin', function(req,res) {
    res.render('admin', {});
})

app.set('view engine', 'ejs');

list = [{"name":"  인기  ","key":"mostPopular","type":"chart"},{"name":"  BJ  ","key":"BJ","type":"search"},{"name":"  음악  ","key":"","type":"","subCategory":[{"name":"순위","key":"PLUjVZgGkfjp8dFsyZkjWJtwzD6tlt2_pL","type":"playlist"},{"name":"최신","key":"PLID4CZACkMJTQGYm6R0Gc4yk_CKZIZKdv","type":"playlist"},{"name":"해외","key":"PLDcnymzs18LWrKzHmzrGH1JzLBqrHi3xQ","type":"playlist"}]},{"name":"  생방  ","key":"PLU12uITxBEPGpEPrYAxJvNDP6Ugx2jmUx","type":"playlist"}];

app.listen(4000, function() {
    console.log('Today\'s Video listening on port 4000!');
})