function btn_submit(cmd) {
    if (cmd == "del") {
        //選択チェック
        var flg1 = false;
        var flg2 = false;
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name == "DEL_CHECK[]") {
                flg1 = true;
                if (document.forms[0].elements[i].checked) {
                    flg2 = true;
                }
            }
        }
        //生成データがない
        if (!flg1) {
            return false;
        }
        //データを選択していない
        if (!flg2) {
            alert("「削除チェックボックス」を選択してください。");
            return true;
        }
        //確認メッセージ
        if (!confirm("{rval MSG103}")) {
            return;
        }
    }
    if (cmd == "execute") {
        if (document.forms[0].month.options.length == 0) {
            return false;
        }

        if (confirm("この処理を実行すると『出欠入力制御日付』以前の出欠入力処理は更新権限がなければ処理できません。\nよろしいですか？")) {
            document.getElementById("marq_msg").style.color = "#FF0000";
        } else {
            return;
        }
    }
    if (cmd == "execute2") {
        cmd = "execute";
        document.forms[0].cmd2.value = "1";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}

function closing_window(flg) {
    if (flg == 1) {
        alert("{rval MSG300}");
    }
    if (flg == 2) {
        alert("{rval MSG305}" + "\n(学期マスタ)");
    }
    closeWin();
    return true;
}

function ConfirmOnError(msg) {
    if (confirm(msg + "\n\nこのまま処理を実行しますか？")) {
        document.getElementById("marq_msg").style.color = "#FF0000";
        document.forms[0].cmd.value = "execute2";
        document.forms[0].submit();
        return false;
    }
}
//「日指定テキストボックス」の値のクリア処理
function clearDay() {
    document.forms[0].day.value = "";
}
