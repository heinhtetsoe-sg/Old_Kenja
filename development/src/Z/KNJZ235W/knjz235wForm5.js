function btn_submit(cmd) {
    //前年度コピー確認
    if (cmd == "copy") {
        if (!confirm("{rval MSG101}")) {
            return false;
        }
    }
    //取消確認
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function setPattern() {
    var isA = document.forms[0].SEQ0011.checked;
    var isB = document.forms[0].SEQ0012.checked;
    var isC = document.forms[0].SEQ0013.checked;
    var isD = document.forms[0].SEQ0014.checked;
    var isE = document.forms[0].SEQ0015.checked;

    var semesterMax = document.forms[0].semesterMax.value;
    var semester = document.forms[0].semester.value;
    var semesterList = semester.split(",");
    for (var i = 0; i < semesterList.length; i++) {
        var no = semesterList[i];
        if (no == semesterMax) continue;
        document.forms[0]["SEQ002" + no].disabled = !(isA || isB);
    }

    document.forms[0].SEQ003.disabled = !(isA || isB || isC);

    document.forms[0].SEQ005.disabled = !(isA || isB);

    var semesterList = semester.split(",");
    for (var i = 0; i < semesterList.length; i++) {
        var no = semesterList[i];
        if (document.forms[0]["SEQ006" + no]) {
            document.forms[0]["SEQ006" + no].disabled = isC || isE;
        }
    }

    document.forms[0].SEQ007.disabled = isC || isE;
    document.forms[0].SEQ008.disabled = isC || isE;
    document.forms[0].SEQ009.disabled = isC || isE;
    document.forms[0].SEQ010.disabled = isD;
    document.forms[0].SEQ011.disabled = !(isD || isE);
}
