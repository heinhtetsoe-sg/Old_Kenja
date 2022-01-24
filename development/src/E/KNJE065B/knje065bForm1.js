function btn_submit(cmd) {
    if (cmd == "recalc" || cmd == "update") {
        var sch_cnt = document.forms[0].SCH_CNT.value;
        var ass_cnt = document.forms[0].ASS_CNT.value;
        if (sch_cnt < 1) {
            closing_window("Sch");
            return false;
        } else if (ass_cnt < 1) {
            closing_window();
            return false;
        }
    }
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }
    if (cmd == "update") {
        if (!confirm("{rval MSG102}")) {
            return false;
        }
        //フレームロック機能（プロパティの値が1の時有効）
        if (document.forms[0].useFrameLock.value == "1") {
            updateFrameLocks();
        }
    }

    document.forms[0].btn_recalc.disabled = true;
    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_can.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}

function closing_window(cd) {
    if (cd == "Rec") {
        alert("{rval MSG305}" + "\n" + "（ 学籍学習記録データ ）");
    } else if (cd == "Sch") {
        alert("{rval MSG305}" + "\n" + "（ 学籍在籍データ ）");
    } else {
        alert("{rval MSG305}" + "\n" + "（ 評定マスタ ）");
    }
    closeWin();
    return true;
}

function sumNum(obj) {
    obj.value = toInteger(obj.value);
    if (obj.value == "") {
        obj.value = 0;
    }
    for (i = 0; i < document.forms[0][obj.name].length; i++) {
        if (obj == document.forms[0][obj.name][i]) {
            var row = i;
            break;
        }
    }
    var objA = document.forms[0]["A_MEMBER[]"];
    var objB = document.forms[0]["B_MEMBER[]"];
    var objC = document.forms[0]["C_MEMBER[]"];
    var objD = document.forms[0]["D_MEMBER[]"];
    var objE = document.forms[0]["E_MEMBER[]"];

    var a = parseInt(objA[row].value, 10);
    var b = parseInt(objB[row].value, 10);
    var c = parseInt(objC[row].value, 10);
    var d = parseInt(objD[row].value, 10);
    var e = parseInt(objE[row].value, 10);

    outputLAYER(row, a + b + c + d + e);
}

function getZero(obj) {
    obj.value = toInteger(obj.value);
    if (obj.value == "") {
        obj.value = 0;
    }
}

function showConfirm() {}
