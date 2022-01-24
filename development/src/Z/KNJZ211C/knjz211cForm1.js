//サブミット
function btn_submit(cmd) {
    //コピー
    if (cmd == 'copy') {
        var this_year   = document.forms[0].THIS_YEAR_CNT.value;
        var pre_year    = document.forms[0].PRE_YEAR_CNT.value;

        if (pre_year == 0) {
            alert('{rval MSG203}'+'\n'+'前年度にデータがありません。');
            return false;
        }

        if (this_year > 0) {
            if (!confirm('対象年度に既にデータが存在します。\n対象年度のデータを削除して、コピーします。よろしいですか？')) {
                return false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
