function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }

    if (cmd == "reset") {
        //取り消し
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    if (cmd == "syukketsu") {
        //出欠の記録参照
        loadwindow("knje012bindex.php?cmd=syukketsu", 0, 350, 600, 280);
        return true;
    } else if (cmd == "subform2") {
        //出欠の記録参照
        loadwindow("knje012bindex.php?cmd=subform2", 0, 350, 600, 280);
        return true;
    } else if (cmd == "shokenlist1") {
        //既入力内容を参照（総合的な学習時間）
        loadwindow("knje012bindex.php?cmd=shokenlist1", 0, 350, 700, 350);
        return true;
    } else if (cmd == "shokenlist2") {
        //既入力内容を参照（総合所見）
        loadwindow("knje012bindex.php?cmd=shokenlist2", 0, 0, 700, 350);
        return true;
    } else if (cmd == "shokenlist3") {
        //既入力内容を参照（出欠の記録備考）
        loadwindow("knje012bindex.php?cmd=shokenlist3", 0, 350, 700, 350);
        return true;
    }
    //更新中の画面ロック(全フレーム)
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == "update") {
            updateFrameLocks();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function reloadIframe(url) {
    document.getElementById("cframe").src = url;
}
