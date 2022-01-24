function btn_submit(cmd) {
    if (cmd == "visionEar1_update") {
        var chkret;
        if (alrtMsg(chkNumber(document.forms[0].R_BAREVISION, "CHKDBL", 4), "裸眼右")) {
            return true;
        }
        if (alrtMsg(chkNumber(document.forms[0].R_VISION, "CHKDBL", 4), "矯正右")) {
            return true;
        }
        if (alrtMsg(chkNumber(document.forms[0].L_BAREVISION, "CHKDBL", 4), "裸眼左")) {
            return true;
        }
        if (alrtMsg(chkNumber(document.forms[0].R_VISION, "CHKDBL", 4), "矯正左")) {
            return true;
        }
        if (alrtMsg(chkNumber(document.forms[0].R_EAR_DB, "CHKINT", 3), "聴力右")) {
            return true;
        }
        if (alrtMsg(chkNumber(document.forms[0].L_EAR_DB, "CHKINT", 3), "聴力左")) {
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm() {
    if (!confirm("{rval MSG106}")) {
        return false;
    }
}

function alrtMsg(chkret, msg) {
    if (chkret < 0) {
        if (chkret == -1) {
            alert("{rval MSG907}" + msg);
        } else if (chkret == -2) {
            alert("{rval MSG913}" + msg);
        }
        return true;
    }
    return false;
}

function chkNumber(obj, chkflg, len) {
    if (typeof obj != "undefined") {
        if (obj.value != null && obj.value != "") {
            if (chkflg == "CHKINT" && !obj.value.match(/^[0-9]+$/)) {
                return -1;
            } else if (chkflg == "CHKDBL" && !obj.value.match(/^[0-9]+$/) && !obj.value.match(/^[0-9]+\.[0-9]+$/)) {
                return -1;
            }
            if (len < obj.value.length) {
                return -2;
            }
        }
    }
    return 1;
}

// 測定困難チェック時
function disabledCantmeasure(obj, div) {
    if (div == "R_VISION") {
        //視力（右）
        if (document.forms[0].R_BAREVISION) {
            document.forms[0].R_BAREVISION.disabled = obj.checked;
        }
        if (document.forms[0].R_VISION) {
            document.forms[0].R_VISION.disabled = obj.checked;
        }
    } else if (div == "L_VISION") {
        //視力（左）
        if (document.forms[0].L_BAREVISION) {
            document.forms[0].L_BAREVISION.disabled = obj.checked;
        }
        if (document.forms[0].L_VISION) {
            document.forms[0].L_VISION.disabled = obj.checked;
        }
    } else if (div == "RL_VISION") {
        //視力（両眼）
        if (document.forms[0].RL_BAREVISION) {
            document.forms[0].RL_BAREVISION.disabled = obj.checked;
        }
        if (document.forms[0].RL_VISION) {
            document.forms[0].RL_VISION.disabled = obj.checked;
        }
    } else if (div == "R_EAR") {
        //聴力（右）
        if (document.forms[0].R_EAR_DB) {
            document.forms[0].R_EAR_DB.disabled = obj.checked;
        }
        if (document.forms[0].R_EAR) {
            document.forms[0].R_EAR.disabled = obj.checked;
        }
    } else if (div == "L_EAR") {
        //聴力（左）
        if (document.forms[0].L_EAR_DB) {
            document.forms[0].L_EAR_DB.disabled = obj.checked;
        }
        if (document.forms[0].L_EAR) {
            document.forms[0].L_EAR.disabled = obj.checked;
        }
    } else {
    }
}
