function btn_submit(cmd) {
    //取消確認
    if (cmd == "clear" && !confirm('{rval MSG106}')) {
        return false;
    }
    //削除確認
    if (cmd == "delete" && !confirm('{rval MSG103}')) {
        return false;
    }

    //更新・削除
    if (cmd == "update" || cmd == "delete") {
        //必須チェック
        if (document.forms[0].CONDITION.value == "") {
            alert('{rval MSG301}\n（状態区分）');
            return true;
        }
        if (document.forms[0].GUIDANCE_PATTERN.value == "") {
            alert('{rval MSG301}\n（指導計画帳票パターン）');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    //必須チェック
    if (document.forms[0].CONDITION.value == "") {
        alert('{rval MSG301}\n（状態区分）');
        return true;
    }
    if (document.forms[0].GUIDANCE_PATTERN.value == "") {
        alert('{rval MSG301}\n（指導計画帳票パターン）');
        return true;
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
