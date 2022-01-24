function btn_submit(cmd) {
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//欠席チェックON/OFFで背景色を黄色表示
function bgcolorYellow(obj, key, receptno) {
    change_flg = true;

    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled = true;

    document.getElementById('ROWID' + key).style.background = obj.checked ? "yellow" : "white";

    //同じ受験番号は使用不可
    for (elCnt = 0; elCnt < document.forms[0].elements.length; elCnt++) {
        var elObj = document.forms[0].elements[elCnt];
        re = new RegExp("^CHK_DATA-" + receptno );
        if (elObj.name.match(re) && elObj.name != obj.name) {
            elObj.disabled = obj.checked ? true : false;
        }
    }
}

//印刷
function newwin(SERVLET_URL) {
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
