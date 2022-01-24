function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";
    if (cmd == "csv1") {
        if (document.forms[0].ASSESS1.value == ""){
            alert('{rval MSG916}');
            return false;
        }
    }
    if (cmd == "csv2") {
        if (!document.forms[0].ASSESS2.value ||
            !document.forms[0].COUNT2.value ||
            !document.forms[0].UNSTUDY2.value ||
            !document.forms[0].ASSESS_AVE2.value)
        {
            alert('{rval MSG916}');
            return false;
        }
    }
    if (cmd == "csv3") {
        if (!document.forms[0].LATE5.value &&
            !document.forms[0].ABSENT5.value &&
            !document.forms[0].SUBCLASS_ABSENT5.value &&
            !document.forms[0].EARLY5.value)
        {
            alert('{rval MSG916}');
            return false;
        }
    }

    if(cmd == "csv1" || cmd == "csv2" || cmd == "csv3") {
        if (document.forms[0].DATE.value < document.forms[0].SDATE.value ||
            document.forms[0].DATE.value > document.forms[0].EDATE.value) {
            alert("日付が学期範囲外です。");
            return;
        }
    }

    if (document.forms[0].DATE.value == "") {
        alert('日付を指定してください。');
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
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
function newwin(SERVLET_URL, schoolCd, fileDiv, cmd) {
    document.forms[0].encoding = "application/x-www-form-urlencoded";

    if (cmd == "csv1") {
        if (document.forms[0].ASSESS1.value == ""){
            alert('{rval MSG916}');
            return false;
        }
        document.forms[0].EXCEL_KIND.value = 1;
    }
    if (cmd == "csv2") {
        if (!document.forms[0].ASSESS2.value ||
            !document.forms[0].COUNT2.value ||
            !document.forms[0].UNSTUDY2.value ||
            !document.forms[0].ASSESS_AVE2.value)
        {
            alert('{rval MSG916}');
            return false;
        }
        document.forms[0].EXCEL_KIND.value = 2;
    }
    if (cmd == "csv3") {
        if (!document.forms[0].LATE5.value &&
            !document.forms[0].ABSENT5.value &&
            !document.forms[0].SUBCLASS_ABSENT5.value &&
            !document.forms[0].EARLY5.value)
        {
            alert('{rval MSG916}');
            return false;
        }
        document.forms[0].EXCEL_KIND.value = 3;
    }
    if (document.forms[0].DATE.value == "") {
        alert('日付を指定してください。');
        return false;
    } else {
        if (document.forms[0].DATE.value < document.forms[0].SDATE.value ||
            document.forms[0].DATE.value > document.forms[0].EDATE.value) {
            alert("日付が学期範囲外です。");
            return;
        }
    }

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
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
