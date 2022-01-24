function btn_submit(cmd) {
    if (cmd == 'visionEar1_update') {
        var chkret;
        if (alrtMsg(chkNumber(document.forms[0].R_BAREVISION, "CHKDBL", 4), '裸眼右')) {
            return true;
        }
        if (alrtMsg(chkNumber(document.forms[0].L_BAREVISION, "CHKDBL", 4), '裸眼左')) {
            return true;
        }
        if (alrtMsg(chkNumber(document.forms[0].R_VISION, "CHKDBL", 4), '矯正右')) {
            return true;
        }
        if (alrtMsg(chkNumber(document.forms[0].R_VISION, "CHKDBL", 4), '矯正左')) {
            return true;
        }
        if (alrtMsg(chkNumber(document.forms[0].R_EAR_DB, "CHKINT", 3), '聴力右')) {
            return true;
        }
        if (alrtMsg(chkNumber(document.forms[0].L_EAR_DB, "CHKINT", 3), '聴力左')) {
            return true;
        }
        if (alrtMsg(chkAge(document.forms[0].DET_REMARK1), '装着開始(才)')) {
            return true;
        }
        if (alrtMsg(chkMonth(document.forms[0].DET_REMARK2), '装着開始(ヵ月)')) {
            return true;
        }
        if (alrtMsg(chkAge(document.forms[0].DET_REMARK3), '人工内耳施術(才)')) {
            return true;
        }
        if (alrtMsg(chkMonth(document.forms[0].DET_REMARK4), '人工内耳施術(ヵ月)')) {
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}

function alrtMsg(chkret, msg) {
    if (chkret < 0) {
        if (chkret == -1) {
            alert('{rval MSG907}'+msg);
        } else if (chkret == -2) {
            alert('{rval MSG913}'+msg);
        }
        return true;
    }
    return false;
}

function chkAge(obj) {
    return chkNumber(obj, "CHKINT", 2);
}
function chkMonth(obj) {
    return chkNumber(obj, "CHKMONTH", 2);
}
function chkNumber(obj, chkflg, len) {
    if (typeof obj != "undefined") {
        if (obj.value !=  null && obj.value != "") {
            if (chkflg == 'CHKINT' && !obj.value.match(/^[0-9]+$/)) {
                return -1;
            } else if (chkflg == 'CHKDBL' && !obj.value.match(/^[0-9]+$/) && !obj.value.match(/^[0-9]+\.[0-9]+$/)) {
                return -1;
            } else if (chkflg == 'CHKMONTH') {
                if (!obj.value.match(/^[0-9]+$/)) {
                    return -1;
                }
                if (parseInt(obj.value) <= 0 || parseInt(obj.value) > 12) {  //1～12の範囲チェック
                    return -2;
                }
            }
            if (len < obj.value.length) {
                return -2;
            }
        }
    }
    return 1;
}
