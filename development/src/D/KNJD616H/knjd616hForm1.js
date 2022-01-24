function btn_submit(cmd) {
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
    //入力チェック
    if (document.forms[0].DATA_DIV[0].checked){
        if (document.forms[0].RANK.value == "") {
            alert('{rval MSG304}' + "\n( 順位 )");
            return false;
        }
        if (document.forms[0].HYOUTEI.value == "") {
            alert('{rval MSG304}' + "\n( 評定平均 )");
            return false;
        }
    } else {
        if (document.forms[0].HYOUTEI_2.value == "") {
            alert('{rval MSG304}' + "\n( 評定２の数 )");
            return false;
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function useOption() {
    if (document.forms[0].DATA_DIV[0].checked){
        document.forms[0].RANK.disabled      = false;
        document.forms[0].HYOUTEI.disabled   = false;
        document.forms[0].HYOUTEI_2.disabled = true;
    } else {
        document.forms[0].RANK.disabled      = true;
        document.forms[0].HYOUTEI.disabled   = true;
        document.forms[0].HYOUTEI_2.disabled = false;
    }
}
