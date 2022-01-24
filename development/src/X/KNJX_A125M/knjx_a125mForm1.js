function btn_submit(cmd) {
    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }
    if (cmd == 'exec') {
        if (document.forms[0].useflg.value == 'sign' && document.forms[0].OUTPUT[0].checked != true && document.forms[0].GRADE_HR_CLASS.value == "") {
            alert('{rval MSG304}\n対象年組ではありません。');
            return true;
        } else if (document.forms[0].OUTPUT[1].checked == true) {
            if (document.forms[0].SHORI_MEI.value == '2' && !confirm('（再確認）削除を開始します。よろしいでしょうか？')) {
                return true;
            }
        } else {
            cmd = "csv";
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//HTTPで自身を呼び出す
function collHttps(requestRoot, cmd) {
    //現在のURL
    urlVal = document.URL;

    //HTTPSをHTTPにする
    setUrl = urlVal.replace("https:", "http:");
    setUrl = setUrl.replace("sign", cmd);
    document.location = setUrl;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return;
}

//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
