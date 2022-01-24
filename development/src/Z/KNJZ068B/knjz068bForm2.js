function btn_submit(cmd) {
    //削除確認
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }

    //取消確認
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    //必須入力チェック
    if (cmd == 'add' || cmd == 'delete') {
        if (document.forms[0].IBSUBCLASS.value == '') {
            alert('{rval MSG301}\n（IB科目）');
            return false;
        }
    }

    //CSV
    if (cmd == 'exec') {
        if (document.forms[0].GETIB_YEAR.value == '') {
            alert('年度を指定してください');
            return false;
        }
        cmd = 'downloadCsv';
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//Unit数チェック
function ibseq(cnt, label) {
    var ibseq;
    ibseq = document.forms[0].IBSEQ_CNT.value;

    if (ibseq > 0) {
    } else {
        alert('{rval MSG901}'+'\n1以上を入力してください。\n( '+label+'数 )');
        document.forms[0].IBSEQ_CNT.focus();
        return false;
    }

    if (ibseq == cnt) {
        return false;
    }

    document.forms[0].cmd.value = 'ibseq';
    document.forms[0].submit();
    return false;
}
