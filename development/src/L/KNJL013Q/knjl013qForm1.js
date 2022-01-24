function btn_submit(cmd) {

    if (cmd == 'update') {
        if (document.forms[0].TESTDIV.value == '') {
            alert('入試区分を指定して下さい。');
            return true;
        }
        if (document.forms[0].RECEPT_CNT.value > 0) {
            if (!confirm('既に実行済みですが、再度実行しますか？\n（再度実行した場合、欠席入力処理以降のデータが初期化されます）')) {
                return true;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
