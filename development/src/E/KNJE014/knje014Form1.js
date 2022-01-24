function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } else if (cmd == 'update') {
        //権限チェック
        if (document.forms[0].AUTH.value < document.forms[0].PASS_AUTH.value) {
            alert('{rval MSG300}');
            return false;
        }

        //フレームロック機能（プロパティの値が1の時有効）
        if (document.forms[0].useFrameLock.value == "1") {
            //更新中の画面ロック
            updateFrameLock()
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//ALLチェック
function check_all(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        var nam = e.name;
        if (e.type == 'checkbox' && nam.match(/COMMENTEX_A_CD./) && !e.disabled) {
            e.checked = obj.checked;
        }
    }
}

//設定チェック
function closeWindow() {
    alert('{rval MSG300}\n帳票で自動処理されます。');
    closeWin();
}
