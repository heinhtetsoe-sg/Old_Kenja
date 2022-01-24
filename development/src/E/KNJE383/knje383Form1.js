function btn_submit(cmd) {
    //実行
    if (cmd == 'execute') {
        //必須チェック
        if (document.forms[0].QUESTIONNAIRECD.value == '') {
            alert('{rval MSG301}\n　　（ 調査名 ）');
            return false;
        }
        if (document.forms[0].ENTRYDATE.value == '') {
            alert('{rval MSG301}\n　　（ 登録日 ）');
            return false;
        }

        var entrydate = document.forms[0].ENTRYDATE.value.split('/');
        var sdate = document.forms[0].SDATE.value.split('/');
        var edate = document.forms[0].EDATE.value.split('/');
        sdate_show = document.forms[0].SDATE.value;
        edate_show = document.forms[0].EDATE.value;

        if ((new Date(eval(sdate[0]),eval(sdate[1])-1,eval(sdate[2])) > new Date(eval(entrydate[0]),eval(entrydate[1])-1,eval(entrydate[2]))) ||
            (new Date(eval(entrydate[0]),eval(entrydate[1])-1,eval(entrydate[2])) > new Date(eval(edate[0]),eval(edate[1])-1,eval(edate[2])))) {
            alert('登録日が入力範囲外です。\n（' + sdate_show + '～' + edate_show + '）');
            return false;
        }

        //確認メッセージ
        if (!confirm('{rval MSG101}')) {
            return;
        }

        //データを格納
        if (document.forms[0].SHOW_SCHOOL_KIND.value == "1") document.forms[0].HIDDEN_SCHOOL_KIND.value = document.forms[0].SCHOOL_KIND.value;
        document.forms[0].HIDDEN_QUESTIONNAIRECD.value  = document.forms[0].QUESTIONNAIRECD.value;
        document.forms[0].HIDDEN_ENTRYDATE.value        = document.forms[0].ENTRYDATE.value;

        //使用不可項目
        if (document.forms[0].SHOW_SCHOOL_KIND.value == "1") document.forms[0].SCHOOL_KIND.disabled = true;
        document.forms[0].QUESTIONNAIRECD.disabled = true;
        document.forms[0].ENTRYDATE.disabled = true;
        document.forms[0].btn_exec.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
