function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";
    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }
    if (document.forms[0].OUTPUT[0].checked == true) {
        if (document.forms[0].SHORI_MEI.value == '2' && !confirm('（再確認）削除を開始します。よろしいでしょうか？')) {
            return true;
        }
    } else if (cmd == 'exec'){
        cmd = "csv";
    }
    if (cmd == 'exec' && document.forms[0].mockRankRangeCnt.value > 0) {
        if (!confirm('作成済み模試データが存在します。\n削除して取込みますがよろしいでしょうか？')) {
            return true;
        }
    }
    if (cmd == 'exec') {
        document.getElementById("marq_msg").style.display = "block";
        document.forms[0].btn_exec.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

//セキュリティーチェック
function OnSecurityError()
{
    alert('{rval MSG300}' + '\n高セキュリティー設定がされています。');
    closeWin();
}

//印刷
function newwin(SERVLET_URL, schoolCd, fileDiv) {
    document.forms[0].encoding = "application/x-www-form-urlencoded";
    if (document.forms[0].OUTPUT[3].checked != true) {
        btn_submit('exec');
    } else {
        //テンプレート格納場所
        urlVal = document.URL;
        urlVal = urlVal.replace("http://", "");
        var resArray = urlVal.split("/");
        var fieldArray = fileDiv.split(":");
        urlVal = "/usr/local/" + resArray[1] + "/src/etc_system/XLS_TEMP_" + schoolCd + "/CSV_Template" + fieldArray[0] + "." + fieldArray[1];
        document.forms[0].TEMPLATE_PATH.value = urlVal;

        action = document.forms[0].action;
        target = document.forms[0].target;

    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
        document.forms[0].action = SERVLET_URL +"/KNJX";
        document.forms[0].target = "_blank";
        document.forms[0].submit();

        document.forms[0].action = action;
        document.forms[0].target = target;
    }
}
