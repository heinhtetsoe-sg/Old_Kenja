function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//HTTPSで自身を呼び出す
function collHttps(requestRoot, cmd, auth) {
    //現在のURL
    urlVal = document.URL;
    //HTTPSをHTTPにする
    setUrl = urlVal.replace("http:", "https:");
    parent.location = setUrl + "&cmd=" + cmd + "&SEND_PRGID=KNJX410&SEND_AUTH=" + auth + "&setUrl=" + setUrl;
}
