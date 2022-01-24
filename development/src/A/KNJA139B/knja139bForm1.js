function btn_submit(cmd) {
    //取消確認
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return false;
    }

    //削除確認
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        } else {
            //フレームロック機能（プロパティの値が1の時有効）
            if (document.forms[0].useFrameLock.value == "1") {
                //更新中の画面ロック
                updateFrameLock()
            }
        }
    }

    //更新
    if (cmd == 'update') {
        //フレームロック機能（プロパティの値が1の時有効）
        if (document.forms[0].useFrameLock.value == "1") {
            //更新中の画面ロック
            updateFrameLock()
        }
    }

    //コピー確認
    if (cmd == "copy" && !confirm('{rval MSG101}')) {
        return false;
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function dataCntCheck(defaultCnt) {
    var cnt = document.forms[0].DATA_CNT.value;

    if (!cnt.match(/^\d{1,2}$/)) {
        alert('{rval MSG909}');
        document.forms[0].DATA_CNT.value = defaultCnt;
        return false;
    }

    if (cnt > 26) {
        alert('{rval MSG915}\n項目数は26以内の値を入力してください')
        document.forms[0].DATA_CNT.value = defaultCnt;
        return false;
    }
    return true;
}