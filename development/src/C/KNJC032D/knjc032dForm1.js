
//KNJC033Dコール
function collHttps(requestRoot, cmd) {
    //現在のURL
    urlVal = document.URL;
    //HTTPをHTTPSにする
    setUrl = urlVal.replace("032", "033");
    setUrl = setUrl.replace("032", "033");
    setUrl = setUrl.replace("032", "033");
    parent.location = setUrl + "&cmd=" + cmd + "&setUrl=" + setUrl;
    window.close();
}
