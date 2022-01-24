function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {

    if (document.forms[0].SEMESTER.value == ""
        || document.forms[0].TESTKIND_ITEMCD.value == ""
        || document.forms[0].GRADE.value == ""
        || document.forms[0].DATE_FROM.value ==""
        || document.forms[0].DATE_TO.value =="") {
        alert('{rval MSG301}' + "出力条件が設定されていません。");
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function chkSuji(obj) {
    if (obj.value !=  null && obj.value != "" && !isNumOnly(obj)) {
        alert('{rval MSG907}' + '数字のみ入力可能です。');
        obj.focus();
        return false;
    }
    if (Number(obj.value) > 10) {
        alert('{rval MSG907}' + '10より大きい値は入力できません。');
        obj.focus();
        return false;
    }
    return true;
}

function isNumOnly(obj) {
    var regex = new RegExp(/^[0-9]+$/);
    return regex.test(obj.value);
}
