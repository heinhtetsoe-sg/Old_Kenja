function btn_submit(cmd) {
    if (cmd == "edit" && document.forms[0].EditedFlg.value == "1") {
        result = confirm("{rval MSG108}");
        if (result == false) {
            var idx = document.forms[0].DispMode.value;
            if (idx != "") {
                document.forms[0]["RADIO"+idx].checked = true;
            }
            return false;
        }
    }
    if (cmd == "delete") {
        result = confirm("{rval MSG103}");
        if (result == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    result = confirm("{rval MSG106}");
    if (result == false) {
        return false;
    }
}

function chgFlg() {
    document.forms[0].EditedFlg.value = "1";
}

function chkInputTxt(obj, Flg) {
    var chkBakStr = obj.value;
    if (Flg == "2") {
        obj.value=toFloat2(obj.value, 1)
    } else {
        obj.value=toInteger(obj.value, 1)
    }
    if (chkBakStr != obj.value) {
        document.forms[0].EditedFlg.value = "1";
    }
}
function chkMax(obj, examno) {
    var chkBakStr = obj.value;
    obj.value=toInteger(obj.value);
    if (chkBakStr != obj.value) {  //入力変化なら終了。
        document.forms[0].EditedFlg.value = "1";
        return;
    }
    document.forms[0].EditedFlg.value = "1";

    var maxPt = document.forms[0]["CD4REMARK1-"+examno].value;
    if (document.forms[0]["CD4REMARK2-"+examno].value != "") {
        if (maxPt == "") {
            maxPt = document.forms[0]["CD4REMARK2-"+examno].value;
        } else if (maxPt < document.forms[0]["CD4REMARK2-"+examno].value) {
            maxPt = document.forms[0]["CD4REMARK2-"+examno].value;
        }
    }
    if (document.forms[0]["CD4REMARK3-"+examno].value != "") {
        if (maxPt == "") {
            maxPt = document.forms[0]["CD4REMARK3-"+examno].value;
        } else if (maxPt < document.forms[0]["CD4REMARK3-"+examno].value) {
            maxPt = document.forms[0]["CD4REMARK3-"+examno].value;
        }
    }

    if (maxPt != "") {
        if (document.getElementById("CD4REMARKX-"+examno)) {
            document.getElementById("CD4REMARKX-"+examno).innerText = "";
        }
        var l101Inf = document.forms[0].optChkArry.value.split(",");
        for (var lCnt = 0;lCnt < l101Inf.length;lCnt++) {
            var subSpl = l101Inf[lCnt].split("#");
            if (subSpl.length > 0) {
                if (parseInt(subSpl[0]) <= maxPt) {
                    if (subSpl.length > 1) {
                        var detSpl = subSpl[1].split("@");
                        document.forms[0]["CD4REMARK4-"+examno].value = detSpl[0];
                        if (detSpl.length > 1) {
                            document.getElementById("CD4REMARKX-"+examno).innerText = detSpl[1];
                            break;
                        }
                    }
                }
            }
        }
    }
}

function toFloat2(checkString, endCntMax) {
    var newString = "";
    var count = 0;
    var flg = false;
    var endCnt = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i + 1);
        if (endCnt >= endCntMax) {
            break;
        }
        if (ch == ".") {
            newString += ch;
            flg = true;
        } else if (ch >= "0" && ch <= "9") {
            newString += ch;
            if (flg) {
                endCnt++;
            }
        }
    }
    return ShowDialog(newString, checkString, "浮動小数点数");
}
function checkClick(obj) {
    document.forms[0][obj.name + "_hidden"].value = obj.checked ? "1" : "0";
    chgFlg();
}
