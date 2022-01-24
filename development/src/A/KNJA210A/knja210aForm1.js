document.onLoad=keyThroughSet()

function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function datecheck(dval)
{
    var chflg = 0;

    //Nullチェック
    if (dval == '') {
        return '';
    }
    //日付正規チェック
    if (!isDate2(dval)) {
       return '';
    }
    //日付の一致チェック
    if (dval == document.forms[0].DEFOULTDATE.value) {
        return '';
    }
    if (sem == 0){
        sem = 1;
        document.forms[0].cmd.value = 'dsub';
        document.forms[0].submit();
        return false;
    }
}

function newwin(SERVLET_URL) {
    //日付入力チェック
    if (document.forms[0].DATE.value == "") {
        alert("日付が不正です。");
        document.forms[0].DATE.focus();
        return false;
    }

    if (!document.forms[0].CHAIRCD.value) {
        alert('{rval MSG916}');
        return false;
    }

    var val = document.forms[0].CHAIRCD.value;
    var tmp = val.split(',');

    document.forms[0].GRADE.value         = tmp[8];
    document.forms[0].HR_CLASS.value      = tmp[9];
    document.forms[0].NAME_SHOW.value     = tmp[2];
    document.forms[0].ATTENDCLASSCD.value = tmp[4];
    document.forms[0].GROUPCD.value       = tmp[5];
    document.forms[0].APPDATE.value       = tmp[7];

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

