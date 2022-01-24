function btn_submit(cmd) {
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    //キーのチェック
    var tmp_staff    = document.forms[0].HID_STAFFCD.value;
    var tmp_maxStamp = document.forms[0].HID_MAX_STAMP_NO.value;
    var tmp_stamp    = document.forms[0].HID_STAMP_NO.value;
    if (cmd == 'new') {
        if (tmp_staff != '') {
        } else {
            alert('職員を左のリストから選択して下さい。');
            return false;
        }
    }
    if (cmd == 'add') {
        if (tmp_staff != '') {
        } else {
            alert('職員を左のリストから選択して下さい。');
            return false;
        }
        if (tmp_maxStamp != '') {
        } else {
            alert('新規印鑑番号を選択して下さい。');
            return false;
        }
    }
    if (cmd == 'update' || cmd == 'delete') {
        if (tmp_staff != '') {
        } else {
            alert('職員を左のリストから選択して下さい。');
            return false;
        }
        if (tmp_stamp != '') {
        } else {
            alert('印鑑番号を上のリストから選択して下さい。');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
function newwin(SERVLET_URL){
    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url + SERVLET_URL +"/KNJA";
    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
function newwin1(SERVLET_URL){
    document.forms[0].FORMID.value = 'KNJA430S_1';
    newwin(SERVLET_URL);
}
function newwin2(SERVLET_URL){
    document.forms[0].FORMID.value = 'KNJA430S_2';
    newwin(SERVLET_URL);
}
