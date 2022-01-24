
//KNJL341Wコール
function collHttps(requestRoot, cmd) {
    //現在のURL
    urlVal = document.URL;
    //HTTPをHTTPSにする
    setUrl = urlVal.replace("340", "341");
    setUrl = setUrl.replace("340", "341");
    setUrl = setUrl.replace("340", "341");
    parent.location = setUrl + "&cmd=" + cmd + "&setUrl=" + setUrl;
    window.close();
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
