
//KNJL361Wコール
function collHttps(requestRoot, cmd) {
    //現在のURL
    urlVal = document.URL;
    //HTTPをHTTPSにする
    setUrl = urlVal.replace("360", "361");
    setUrl = setUrl.replace("360", "361");
    setUrl = setUrl.replace("360", "361");
    parent.location = setUrl + "&cmd=" + cmd + "&setUrl=" + setUrl;
    window.close();
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
