function btn_submit(cmd) {
    //取消
    if (cmd == "reset" && !confirm("{rval MSG106}")) return true;

    if (cmd == "delete") {
        result = confirm("{rval MSG103}");
        if (result == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//HTTPSで自身を呼び出す
function collHttps() {
    //現在のURL
    setUrl = document.URL;

    //呼び出したい機能IDに変換
    setUrl = setUrl.replace("KNJL677I", "KNJL671I");
    setUrl = setUrl.replace("knjl677i", "knjl671i");
    //呼び出し元IDと処理を設定
    setUrl += "&cmd=";
    setUrl += "&cmd=&SEND_PRGID=KNJL677I";
    setUrl += "&SEND_CMD=search"; //処理 insert, update, updateAll, search
    //参照画面の為、更新対象項目の設定は不要

    parent.location = setUrl;
    window.close();
}
