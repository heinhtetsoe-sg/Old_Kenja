function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";

    if (cmd == 'output') {
        var val_year = document.forms[0].YEAR.value;
        var val_scho = document.forms[0].SCHOOLCD.value;
        var val_majo = document.forms[0].MAJORCD.value;
        var val_saik = document.forms[0].SAIKEN.value;
        if (val_year == '' || 
            val_scho == '' || 
            val_majo == '') {
            alert('{rval MSG301}');
            return false;
        }
        if (document.forms[0].RADIO[0].checked && val_saik == '') {
            alert('{rval MSG301}');
            return false;
        }
        if (val_year.length != '4' || 
            val_scho.length != '5' || 
            val_majo.length != '3') {
            alert('{rval MSG901}\n桁数が違います。全桁入力して下さい。');
            return false;
        }
        if (document.forms[0].RADIO[0].checked && val_saik.length != '3') {
            alert('{rval MSG901}\n桁数が違います。全桁入力して下さい。');
            return false;
        }
    }

    if (cmd == 'check') {
        if (document.forms[0].YEAR.value.length != '4') {
            alert(年度の項目を入力して下さい。);
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function txt_disable(obj) {
    if (obj.value == 1) document.forms[0].SAIKEN.disabled = false;
    if (obj.value == 2) document.forms[0].SAIKEN.disabled = true;
}

function OnAuthError() {
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
