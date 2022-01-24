function btn_submit(cmd) {
    if (cmd == "copy") {
        var copyMotoCnt = document.forms[0].COPY_MOTO_CNT.value;
        var copySakiCnt = document.forms[0].COPY_SAKI_CNT.value;
        var copyMotoCd = document.forms[0].PRE_TESTCD.value;
        var copySakiCd = document.forms[0].TESTCD.value;

        //①確認メッセージを表示する。
        if (copySakiCnt == 0) {
            if (!confirm('コピーします。よろしいでしょうか？')) {
                return false;
            }
        } else {
            if (!confirm('既に対象データが存在します。\n対象データを削除して、コピーします。よろしいでしょうか？')) {
                return false;
            }
        }
        //②エラーメッセージを表示する。
        if (copyMotoCd == copySakiCd) {
            alert('コピーできません。\n\n選択した参照データが対象データと同じです。\n別の参照データを選択して下さい。');
            return false;
        }
        //③エラーメッセージを表示する。
        if (copyMotoCnt == 0) {
            alert('コピーできません。\n\n選択した参照データが存在しません。\n別の参照データを選択して下さい。');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
