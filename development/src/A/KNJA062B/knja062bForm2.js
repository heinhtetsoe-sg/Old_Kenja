function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";
    if (cmd == "delete") {
        if (!confirm("{rval MSG103}")) return false;
    }
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) {
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    if (cmd == "exec") {
        if (
            document.forms[0].OUTPUT[1].checked &&
            document.forms[0].FILE.value == ""
        ) {
            alert("ファイルを指定してください");
            return false;
        }

        if (document.forms[0].OUTPUT[0].checked) {
            cmd = "downloadHead";
        } else if (document.forms[0].OUTPUT[1].checked) {
            cmd = "uploadCsv";
        } else if (document.forms[0].OUTPUT[2].checked) {
            cmd = "downloadCsv";
        } else if (document.forms[0].OUTPUT[3].checked) {
            cmd = "downloadError";
        } else {
            alert("ラジオボタンを選択してください。");
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}

function changeRadio(obj) {
    var type_file;
    if (obj.value == "1") {
        //1は取り込み
        document.forms[0].FILE.disabled = false;
    } else {
        document.forms[0].FILE.disabled = true;
        type_file = document.getElementById("type_file"); //ファイルアップローダーの値を消す
        var innertString = type_file.innerHTML;
        type_file.innerHTML = innertString;
    }
}

//印刷
function newwin(SERVLET_URL, schoolCd, fileDiv) {
    document.forms[0].encoding = "application/x-www-form-urlencoded";
    if (document.forms[0].OUTPUT[2].checked != true) {
        btn_submit("exec");
    } else {
        //テンプレート格納場所
        urlVal = document.URL;
        urlVal = urlVal.replace("http://", "");
        var resArray = urlVal.split("/");
        var fieldArray = fileDiv.split(":");
        urlVal =
            "/usr/local/" +
            resArray[1] +
            "/src/etc_system/XLS_TEMP_" +
            schoolCd +
            "/CSV_Template" +
            fieldArray[0] +
            "." +
            fieldArray[1];
        document.forms[0].TEMPLATE_PATH.value = urlVal;

        action = document.forms[0].action;
        target = document.forms[0].target;

        //    url = location.hostname;
        //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
        document.forms[0].action = SERVLET_URL + "/KNJA";
        document.forms[0].target = "_blank";
        document.forms[0].submit();

        document.forms[0].action = action;
        document.forms[0].target = target;
    }
}
