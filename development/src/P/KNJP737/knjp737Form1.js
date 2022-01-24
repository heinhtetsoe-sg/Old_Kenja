function btn_submit(cmd) {
    if(cmd == 'copy'){
        if (!confirm('{rval MSG101}')) {
            alert('{rval MSG203}');
            return false;
        }
    }
    //取消
    if (cmd == "clear") {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //削除
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }

    //更新
    if (cmd == "update") {
        //反映された項目数で処理
        if (document.forms[0].ROW.value != document.forms[0].ROW_NO_CNT.value) {
            document.forms[0].ROW_NO_CNT.value = document.forms[0].ROW.value;
        }
    }

    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }
    if (cmd == 'exec') {
        //データ取込
        var e = document.getElementById('marq_msg');
        e.style.color = '#FF0000';
        e.style.fontWeight = '400';
        e.innerHTML = '処理中です...しばらくおまちください';

        document.forms[0].btn_exec.disabled = true;
        document.forms[0].btn_ref.disabled = true;
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_delete.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//開始列番号・項目数チェック
function reflect() {
    var s_row_no   = parseInt(document.forms[0].S_ROW_NO.value,10);
    var row_no_cnt = parseInt(document.forms[0].ROW_NO_CNT.value,10);
    var btn_udpate = document.forms[0].btn_udpate;

    if (s_row_no > 0) {
    } else {
        alert('{rval MSG913}'+'\n( 開始列番号：1以上 )');
        document.forms[0].S_ROW_NO.focus();
        btn_udpate.disabled = true;
        return false;
    }
    if (row_no_cnt > 0) {
    } else {
        alert('{rval MSG913}'+'\n( 項目数：1以上 )');
        document.forms[0].ROW_NO_CNT.focus();
        btn_udpate.disabled = true;
        return false;
    }

    if (s_row_no > 1000) {
        alert('{rval MSG913}'+'\n開始列番号は1000を超えてはいけません。');
        document.forms[0].S_ROW_NO.focus();
        btn_udpate.disabled = true;
        return false;
    }
    if (row_no_cnt > 1000) {
        alert('{rval MSG913}'+'\n項目数は1000を超えてはいけません。');
        document.forms[0].ROW_NO_CNT.focus();
        btn_udpate.disabled = true;
        return false;
    }
    if ((s_row_no + row_no_cnt) > 1000) {
        alert('{rval MSG913}'+'\n列番号は1000を超えてはいけません。');
        document.forms[0].ROW_NO_CNT.focus();
        btn_udpate.disabled = true;
        return false;
    }

    document.forms[0].cmd.value = 'reflect';
    document.forms[0].submit();
    return false;
}
