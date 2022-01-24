function btn_submit(cmd) {

    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    result = confirm('{rval MSG107}');
    if (result == false) {
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    //必須チェック
    if (document.forms[0].SCHREGNO.value == '') {
        alert('{rval MSG310}\n( 氏名 )');
        return;
    }

    if (document.forms[0].SUBCLASSCD.value == '' || document.forms[0].SUBCLASSCD.value == "---") {
        alert('{rval MSG310}\n( 科目 )');
        return;
    }


    action = document.forms[0].action;
    target = document.forms[0].target;

    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJM";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
