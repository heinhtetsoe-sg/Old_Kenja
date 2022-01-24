function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_EXAMNO.value.length == 0) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if ((document.forms[0].TESTDIV.disabled) || (document.forms[0].EXAMHALLCD.disabled)) {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//印刷
function newwin(SERVLET_URL, cmd){
    action = document.forms[0].action;
    target = document.forms[0].target;
    var oldcmd = document.forms[0].cmd.value;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
    document.forms[0].cmd.value = oldcmd.value;
}

function showConfirm() {
    if(confirm('{rval MSG106}')) return true;
    return false;
}

//チェックon処理
function CheckOn(obj, flg) {
    var arrData = new Array();
    arrData = obj.id.split("-");
    var checkTesAb = document.getElementById("TEST_ABSENCE-" + arrData[1]);
    var checkBakAb = document.getElementById("BAK_ABSENCE-" + arrData[1]);

    document.forms[0].HID_TESTDIV.value      = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_EXAMHALLCD.value   = document.forms[0].EXAMHALLCD.options[document.forms[0].EXAMHALLCD.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled      = true;
    document.forms[0].EXAMHALLCD.disabled   = true;

    if ((checkTesAb.checked == true && flg == "1")) {
        document.getElementById('ROWID' + arrData[1]).style.background = "red";
    }

    if ((checkTesAb.checked == false && checkBakAb.value == "3") || (checkTesAb.checked == true && checkBakAb.value != "3")) {
        document.getElementById('ROWID' + arrData[1]).style.background = "yellow";
    }

    if ((checkTesAb.checked == false && checkBakAb.value != "3") || (checkTesAb.checked == true && checkBakAb.value == "3")) {
        document.getElementById('ROWID' + arrData[1]).style.background = "white";
    }

    return;
}
