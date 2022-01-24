function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_RECEPTNO.value.length == 0) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if (document.forms[0].TESTDIV.disabled) {
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

function showConfirm() {
    if(confirm('{rval MSG106}')) return true;
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    //必須チェック
    if (document.forms[0].TESTDIV.value == '') {
        alert('{rval MSG310}\n( 志望区分 )');
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function Setflg(obj){
    change_flg = true;
    document.forms[0].HID_TESTDIV.value         = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;
    document.forms[0].HID_HOPE_COURSECODE.value      = document.forms[0].HOPE_COURSECODE.options[document.forms[0].HOPE_COURSECODE.selectedIndex].value;

    document.forms[0].TESTDIV.disabled          = true;
    document.forms[0].HOPE_COURSECODE.disabled       = true;
    document.forms[0].FINSCHOOLCD.disabled         = true;
    document.forms[0].btn_read.disabled         = true;
    var tmpstr = obj.id.split("-");
    if (tmpstr.length > 1) {
        if (obj.checked) {
            document.getElementById('ROWID' + tmpstr[1]).style.background="yellow";
            obj.style.background="yellow";
        } else {
            document.getElementById('ROWID' + tmpstr[1]).style.background="white";
            obj.style.background="white";
        }
    }
}
