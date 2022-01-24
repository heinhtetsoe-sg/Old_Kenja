
function btn_submit(cmd) {

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    } else if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } else if (cmd == 'listdelete') {
        var checkLine = "";
        var sep = "";
        var checkList = document.forms[0].DEL_CHECK;
        if (checkList) {
            if (checkList.length) {
                for (let i = 0; i < checkList.length; i++) {
                    var check = checkList[i];
                    if (check.checked) {
                        checkLine += sep + check.value;
                        sep = ",";
                    }
                }
            } else {
                //1行しかない場合
                if (checkList.checked) {
                    checkLine = checkList.value;
                }
            }
        }
        //チェックされている行を取得
        document.forms[0].DEL_LIST.value = checkLine;

        if (checkLine == "") {
            alert('{rval MSG304}');
            return true;
        }
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function listSet(semester, subclassCd, unitCd) {

    if (document.forms[0].SEMESTER) {
        document.forms[0].SEMESTER.value = semester;
    }
    if (document.forms[0].SUBCLASSCD) {
        document.forms[0].SUBCLASSCD.value = subclassCd;
    }
    if (document.forms[0].UNITCD) {
        document.forms[0].UNITCD.value = unitCd;
    }

    document.forms[0].cmd.value = "edit";
    document.forms[0].submit();
    return false;
}

setTimeout(function () {
    window.onload = new function () {
        if (sessionStorage.getItem("KNJD420LForm1_CurrentCursor") != null) {
            document.title = "";
            document.getElementById(sessionStorage.getItem("KNJD420LForm1_CurrentCursor")).focus();
        } else if (sessionStorage.getItem("link_click") == "right_screen") {
            document.getElementById("rightscreen").focus();
            sessionStorage.removeItem('link_click');
        } else {
            document.title = "右情報画面";
        }
    }
}, 800);

function current_cursor(para) {
    sessionStorage.setItem("KNJD420LForm1_CurrentCursor", para);
}
