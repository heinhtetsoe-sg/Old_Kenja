function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    if (cmd == 'houkoku' || cmd == 'update') {
        if (document.forms[0].SCHOOLCD.value == "") {
            alert('教育委員会統計用学校番号が、未登録です。');
            return false;
        }
    }
    if (cmd == 'houkoku') {
        if (document.forms[0].FIXED_DATA.value == "") {
            alert('{rval MSG304}'+'(確定データ)');
            return false;
        }
        if (document.forms[0].DOC_NUMBER.value == "") {
            alert('{rval MSG304}'+'(文書番号)');
            return false;
        }
        if (document.forms[0].EXECUTE_DATE.value == "") {
            alert('{rval MSG304}'+'(作成日)');
            return false;
        }
        if (!confirm('{rval MSG108}')) return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

//印刷
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function fixed(REQUESTROOT){

    load  = "loadwindow('"+ REQUESTROOT +"/E/KNJE441/knje441index.php?cmd=fixedLoad";
    load += "',400,250,450,250)";

    eval(load);
}
