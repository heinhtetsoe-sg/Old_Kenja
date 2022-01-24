function btn_submit(cmd) {
    if (cmd == "clear") {
        result = confirm("{rval MSG106}");
        if (result == false) {
            return false;
        }
    }
    if (cmd == "update" || cmd == "sim" || cmd == "standard") {
        if (
            (cmd == "sim" || cmd == "standard") &&
            document.forms[0].COUNT.value == ""
        ) {
            alert("{rval MSG305}" + "( 序列確定処理 )");
            return false;
        }
        if (cmd == "update" && document.forms[0].SIM_FLG.value == "off") {
            alert(
                "確定するデータが存在しません。シミュレーションを実行して下さい。"
            );
            return false;
        }
        if (document.forms[0].SUBCLASSCD.value == "") {
            alert("{rval MSG301}");
            return false;
        }
        if (cmd == "update" || cmd == "sim") {
            for (var i = 0; i < document.forms[0].length; i++) {
                if (
                    document.forms[0][i].name.match(/ASSESS(LOW|HIGH)/) &&
                    !document.forms[0][i].name.match(/STANDARD_ASSESSLOW/)
                ) {
                    if (document.forms[0][i].value == "") {
                        alert("{rval MSG301}");
                        return false;
                    }
                }
            }
            for (var i = 2; i <= 5; i++) {
                lowValue =
                    document.forms[0].elements["ASSESSLOW" + (i - 1)].value;
                nowValue = document.forms[0].elements["ASSESSLOW" + i].value;
                if (parseInt(lowValue) >= parseInt(nowValue)) {
                    alert("下限値が昇順に並んでいません。");
                    return false;
                }
            }
        }
    }
    if (cmd == "copy") {
        result = confirm("{rval MSG102}");
        if (result == false) {
            return false;
        }
    }
    //読み込み中は、更新系ボタンをグレー（押せないよう）にする。
    document.forms[0].btn_copy.disabled = true;
    document.forms[0].btn_keisan.disabled = true;
    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_standard.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//上限値セット
function isNumb(that, level, mode) {
    var num;
    var anser;

    that.value = toInteger(that.value);
    if (that.value <= 0) {
        anser = "";
    } else {
        if (mode == "SYOUSUU") {
            anser = that.value;
            anser = (anser * 10 - 1) / 10;
            anser = "" + anser;
            if (anser.length == 1) {
                anser = anser + ".0";
            }
        } else {
            anser = that.value - 1;
        }
    }
    document.getElementById("strID" + level).innerHTML = anser;
    document.forms[0].elements["ASSESSHIGH" + level].value = anser;

    lowValue = document.forms[0].elements["ASSESSLOW" + level].value;
    nowValue = document.forms[0].elements["ASSESSLOW" + (level + 1)].value;
    if (lowValue != "") {
        if (parseInt(nowValue) < parseInt(lowValue)) {
            document.getElementById("strID" + level).innerHTML = "";
            document.forms[0].elements["ASSESSHIGH" + level].value = "";
        }
    } else {
        if (nowValue != "") {
            nowValue--;
        }
        document.getElementById("strID" + level).innerHTML = nowValue;
        document.forms[0].elements["ASSESSHIGH" + level].value = nowValue;
    }

    return;
}

function closing_window() {
    alert("{rval MSG300}");
    closeWin();
    return true;
}
