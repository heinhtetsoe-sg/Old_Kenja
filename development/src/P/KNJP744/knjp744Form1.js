function btn_submit(cmd) {
    if (cmd == "copy") {
        //処理開始確認
        if (!confirm("{rval MSG101}")) {
            alert("{rval MSG203}");
            return;
        }

        if (document.forms[0].dataCnt.value == 0) {
            alert("{rval MSG203}\nコピー可能なデータが存在しません。");
            return;
        }

        //処理年度にデータ有だが実行するか確認
        if (document.forms[0].exist_flg.value != 0) {
            if (!confirm("{rval MSG104}")) {
                alert("{rval MSG203}");
                return;
            }
        }

        if (document.forms[0].err_flg.value == true) {
            var msg = "参照年度からコピーされないデータが存在します。\n";
            msg = msg + "処理を続行しますか？";
            if (!confirm(msg)) {
                alert("{rval MSG203}");
                return;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
