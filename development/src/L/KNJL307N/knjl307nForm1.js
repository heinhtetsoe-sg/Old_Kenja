function btn_submit(cmd) {
    if (cmd == 'exec') {
        if (document.forms[0].OUTPUT[0].checked == true) {
            if (document.forms[0].TOTALCD.value == "9999") {
                alert('{rval MSG300}' + '\n全コースは指定できません。');
                return false;
            }
            if (document.forms[0].DATADIV.value == "3") {
                alert('{rval MSG300}' + '\n1:願書、2:調査書のみ');
                return false;
            }
            cmd = 'head';
        }
        if (document.forms[0].OUTPUT[1].checked == true) {
            if (document.forms[0].TOTALCD.value == "9999") {
                alert('{rval MSG300}' + '\n全コースは指定できません。');
                return false;
            }
            if (document.forms[0].DATADIV.value == "3") {
                alert('{rval MSG300}' + '\n1:願書、2:調査書のみ');
                return false;
            }
            if (document.forms[0].APPLICANTDIV.value == "") {
                alert('{rval MSG301}' + '\n入試制度');
                return false;
            }
            if (document.forms[0].TESTDIV.value == "") {
                alert('{rval MSG301}' + '\n入試区分');
                return false;
            }
            if (document.forms[0].TOTALCD.value == "") {
                alert('{rval MSG301}' + '\n志望区分');
                return false;
            }
        }
        if (document.forms[0].OUTPUT[2].checked == true) {
            if (document.forms[0].TOTALCD.value == "9999") {
                alert('{rval MSG300}' + '\n全コースは指定できません。');
                return false;
            }
            if (document.forms[0].DATADIV.value == "3") {
                alert('{rval MSG300}' + '\n1:願書、2:調査書のみ');
                return false;
            }
            cmd = 'error';
        }
        if (document.forms[0].OUTPUT[3].checked == true) {
            if (document.forms[0].APPLICANTDIV.value == "") {
                alert('{rval MSG301}' + '\n入試制度');
                return false;
            }
            if (document.forms[0].TESTDIV.value == "") {
                alert('{rval MSG301}' + '\n入試区分');
                return false;
            }
            if (document.forms[0].TOTALCD.value == "") {
                alert('{rval MSG301}' + '\n志望区分');
                return false;
            }
            cmd = 'data';
        }
    }
    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }
    if (document.forms[0].OUTPUT[1].checked == true) {
        if (document.forms[0].SHORI_MEI.value == '2' && !confirm('（再確認）削除を開始します。よろしいでしょうか？')) {
            return true;
        }
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