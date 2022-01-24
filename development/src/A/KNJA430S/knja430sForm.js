function btn_submit(cmd) {
    
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//HTTPSで自身を呼び出す
function collHttps(requestRoot, cmd) {
    //現在のURL
    urlVal = document.URL;
    //HTTPをHTTPSにする
    setUrl = urlVal.replace("http", "http");
//    setUrl = urlVal.replace("http", "https");
    parent.location = setUrl + "&cmd=" + cmd;
    window.close();
}
function ShowConfirm(){
    if (!confirm('{rval MSG106}'))
        return false;
}
//権限
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}