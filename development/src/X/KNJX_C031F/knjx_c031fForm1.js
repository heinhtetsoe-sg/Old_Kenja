/* Add by HPA for current_cursor start 2020/02/03 */
window.onload = function () {
    if (sessionStorage.getItem("KNJX_C031fForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJX_C031fForm1_CurrentCursor")).focus();
    }
}

function current_cursor(para) {
  sessionStorage.setItem("KNJX_C031fForm1_CurrentCursor", para);
}
/* Add by HPA for current_cursor end 2020/02/20 */

function btn_submit(cmd) {
  /* Add by HPA for current_cursor start 2020/02/03 */
  if (sessionStorage.getItem("KNJX_C031fForm1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJX_C031fForm1_CurrentCursor")).blur();
  }
  /* Add by HPA for current_cursor end 2020/02/20 */
    document.forms[0].encoding = "multipart/form-data";

    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }
    if (document.forms[0].OUTPUT[1].checked == true) {
        if (document.forms[0].SHORI_MEI.value == '2' && !confirm('（再確認）削除を開始します。よろしいでしょうか？')) {
            return true;
        }
    } else if (cmd != "" && cmd != "change_radio") {
        cmd = "csv";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

//セキュリティーチェック
function OnSecurityError() {
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

//        url = location.hostname;
//        document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
        document.forms[0].action = SERVLET_URL +"/KNJX";
        document.forms[0].target = "_blank";
        document.forms[0].submit();

        document.forms[0].action = action;
        document.forms[0].target = target;
    }
}
