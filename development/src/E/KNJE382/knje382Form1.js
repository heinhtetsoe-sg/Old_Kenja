function btn_submit(cmd) {
    //実行
    if (cmd == 'execute') {
        //必須チェック
        if (document.forms[0].COURSE_KIND.value == '') {
            alert('{rval MSG301}\n　　（ 進路種別 ）');
            return false;
        }
        if (document.forms[0].FROM_QUESTIONNAIRECD.value == '') {
            alert('{rval MSG301}\n　　（ コピー元調査名 ）');
            return false;
        }
        if (document.forms[0].TO_QUESTIONNAIRECD.value == '') {
            alert('{rval MSG301}\n　　（ コピー先調査名 ）');
            return false;
        }
        if (document.forms[0].TO_ENTRYDATE.value == '') {
            alert('{rval MSG301}\n　　（ コピー先登録日 ）');
            return false;
        }

        var to_entrydate = document.forms[0].TO_ENTRYDATE.value.split('/');
        var sdate = document.forms[0].SDATE.value.split('/');
        var edate = document.forms[0].EDATE.value.split('/');
        sdate_show = document.forms[0].SDATE.value;
        edate_show = document.forms[0].EDATE.value;

        if ((new Date(eval(sdate[0]),eval(sdate[1])-1,eval(sdate[2])) > new Date(eval(to_entrydate[0]),eval(to_entrydate[1])-1,eval(to_entrydate[2]))) ||
            (new Date(eval(to_entrydate[0]),eval(to_entrydate[1])-1,eval(to_entrydate[2])) > new Date(eval(edate[0]),eval(edate[1])-1,eval(edate[2])))) {
            alert('コピー先登録日が入力範囲外です。\n（' + sdate_show + '～' + edate_show + '）');
            return false;
        }

        if (document.forms[0].FROM_QUESTIONNAIRECD.value == document.forms[0].TO_QUESTIONNAIRECD.value) {
            alert('コピー元調査名とコピー先調査名が同じです。');
            return false;
        }

        //確認メッセージ
        if (!confirm('{rval MSG101}')) {
            return;
        }

        //データを格納
        if (document.forms[0].SHOW_SCHOOL_KIND.value == "1") document.forms[0].HIDDEN_SCHOOL_KIND.value = document.forms[0].SCHOOL_KIND.value;
        document.forms[0].HIDDEN_COURSE_KIND.value          = document.forms[0].COURSE_KIND.value;
        document.forms[0].HIDDEN_FROM_QUESTIONNAIRECD.value = document.forms[0].FROM_QUESTIONNAIRECD.value;
        document.forms[0].HIDDEN_TO_QUESTIONNAIRECD.value   = document.forms[0].TO_QUESTIONNAIRECD.value;
        document.forms[0].HIDDEN_TO_ENTRYDATE.value         = document.forms[0].TO_ENTRYDATE.value;

        //使用不可項目
        if (document.forms[0].SHOW_SCHOOL_KIND.value == "1") document.forms[0].SCHOOL_KIND.disabled = true;
        document.forms[0].COURSE_KIND.disabled = true;
        document.forms[0].FROM_QUESTIONNAIRECD.disabled = true;
        document.forms[0].TO_QUESTIONNAIRECD.disabled = true;
        document.forms[0].TO_ENTRYDATE.disabled = true;
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
