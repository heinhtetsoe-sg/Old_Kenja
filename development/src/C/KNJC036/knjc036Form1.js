function btn_submit(cmd) {
    if (cmd=='update' || cmd == 'reset') {
        if (document.forms[0].SEMESTER.value=="" || document.forms[0].HR_CLASS.value=="" ) {
            alert('{rval MSG304}');
            return;
        }
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return;
        }
    }
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function checkText(obj) {
    //数字チェック
    if (isNaN(obj.value)) {
        alert('{rval MSG907}');
        obj.value = obj.defaultValue;
        return;
    }

    var score = parseInt(obj.value);
    if (score < -99 || score > 99) {
        alert('{rval MSG913}'+'『 -99 ～ 99 』まで入力可能です。');
        obj.value = obj.defaultValue;
        return;
    }
}
function newwin(SERVLET_URL) {
    if (document.forms[0].SEMESTER.value=="" || document.forms[0].HR_CLASS.value=="" ) {
        alert('{rval MSG304}');
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
