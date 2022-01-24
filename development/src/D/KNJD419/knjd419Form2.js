function btn_submit(cmd) {
    //取消
    if (cmd == "clear" && !confirm('{rval MSG106}')) {
        return false;
    }
    //削除
    if (cmd == "delete" && !confirm('{rval MSG103}')) {
        return false;
    }

    //更新
    if (cmd == "update") {
        //必須チェック
        if (document.forms[0].UNIT_AIM_DIV.value == '1' && document.forms[0].UNITCD.value == "") {
            alert('{rval MSG310}\n　（ 単元 ）');
            return false;
        }

        remark = "";
        var inputs = document.getElementsByTagName("textarea");
        for (i = 0; i < inputs.length; i++) {
            remark += inputs[i].value;
        }
        //所見入力チェック
        if (remark == '') {
            if (!confirm('{rval MSG102}\n所見が未入力です。')) {
                return false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//項目数範囲チェック
function cnt_check() {
    cnt = document.forms[0].GROUP_REMARK_CNT.value;

    if (cnt == "" || cnt < 1 || cnt > 50) {
        alert('{rval MSG916}\n1～50を入力してください。');
        document.forms[0].GROUP_REMARK_CNT.focus();
        return false;
    }
    document.forms[0].setcnt.value = cnt;

    document.forms[0].cmd.value = 'edit2';
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {

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
