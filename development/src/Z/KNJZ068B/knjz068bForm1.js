function btn_submit(cmd) {
    //コピー確認
    if (cmd == 'copy'){
        if (!confirm('{rval MSG102}\n（前年度コピー）'))
            return false;
        //存在チェック（前年度）
        cnt_pre_year = document.forms[0].cnt_pre_year.value;
        if (cnt_pre_year == 0) {
            alert('前年度のデータが存在しません。');
            return false;
        }
        //存在チェック（対象年度）
        cnt_ibyear = document.forms[0].cnt_ibyear.value;
        if (cnt_ibyear > 0) {
            if (!confirm('対象年度にデータが存在します。\n更新してもよろしいでしょうか？'))
                return false;
        }
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
