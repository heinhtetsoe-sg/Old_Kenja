function btn_submit(cmd) {
    if (cmd == "delete" && !confirm("{rval MSG103}")) {
        return true;
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].left_select.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
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
//未検査項目設定
function setNotExamined(notFieldSet) {
    var fieldArr = new Array();
    fieldArr = notFieldSet.split(":");

    for (var i = 0; i < fieldArr.length; i++) {
        fieldName = document.forms[0][fieldArr[i]];
        if (fieldName) {
            fieldName.style.backgroundColor = "#cccccc";
            if (fieldName.type == "select-one") {
                for (var j = 0; j < fieldName.length; j++) {
                    if (fieldName.options[j].selected) {
                    } else {
                        fieldName.options[j].disabled = true;
                    }
                }
            } else if (fieldName.type == "checkbox") {
                fieldName.checked = false;
                fieldName.disabled = true;
            } else {
                fieldName.readOnly = true;
            }
        }
    }

    return false;
}

function check_all(obj) {
    var ii = 0;
    var fixobjname = "RCHECK";
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.indexOf(fixobjname) === 0) {
            if (document.forms[0].elements[i].name.slice(6).search(/^[0-9]+$/) === 0) {
                document.forms[0].elements[i].checked = obj.checked;
                ii++;
            }
        }
    }
}
function doSubmit() {
    var ii = 0;
    var rcheckArray = new Array();
    var checkFlag = false;
    var fixobjname = "RCHECK";
    for (var iii = 0; iii < document.forms[0].elements.length; iii++) {
        if (document.forms[0].elements[iii].name.indexOf(fixobjname) === 0) {
            if (document.forms[0].elements[iii].name.slice(6).search(/^[0-9]+$/) === 0) {
                rcheckArray.push(document.forms[0].elements[iii]);
                ii++;
            }
        }
    }
    for (var k = 0; k < rcheckArray.length; k++) {
        if (rcheckArray[k].checked) {
            checkFlag = true;
            break;
        }
    }
    if (!checkFlag) {
        alert("最低ひとつチェックを入れてください。");
        return false;
    }

    alert("{rval MSG102}");
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].left_select.length == 0 && document.forms[0].right_select.length == 0) {
        alert("{rval MSG916}");
        return false;
    }
    for (var i = 0; i < document.forms[0].left_select.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = "replace_update1";
    document.forms[0].submit();
    return false;
}
function temp_clear() {
    ClearList(document.forms[0].left_select, document.forms[0].left_select);
    ClearList(document.forms[0].right_select, document.forms[0].right_select);
}
// 視力・測定困難チェック時
function disVision(obj, div) {
    if (div == "right") {
        if (document.getElementById("R_BAREVISION")) {
            document.getElementById("R_BAREVISION").disabled = obj.checked;
        }
        if (document.getElementById("R_VISION")) {
            document.getElementById("R_VISION").disabled = obj.checked;
        }
        if (document.forms[0].R_BAREVISION_MARK) {
            document.forms[0].R_BAREVISION_MARK.disabled = obj.checked;
        }
        if (document.forms[0].R_VISION_MARK) {
            document.forms[0].R_VISION_MARK.disabled = obj.checked;
        }
    } else {
        if (document.getElementById("L_BAREVISION")) {
            document.getElementById("L_BAREVISION").disabled = obj.checked;
        }
        if (document.getElementById("L_VISION")) {
            document.getElementById("L_VISION").disabled = obj.checked;
        }
        if (document.forms[0].L_BAREVISION_MARK) {
            document.forms[0].L_BAREVISION_MARK.disabled = obj.checked;
        }
        if (document.forms[0].L_VISION_MARK) {
            document.forms[0].L_VISION_MARK.disabled = obj.checked;
        }
    }
}
// 聴力・測定困難チェック時
function disEar(obj, div) {
    if (div == "right") {
        if (document.forms[0].R_EAR_DB) {
            document.forms[0].R_EAR_DB.disabled = obj.checked;
        }
        if (document.forms[0].R_EAR) {
            document.forms[0].R_EAR.disabled = obj.checked;
        }
        if (document.forms[0].R_EAR_DB_1000) {
            document.forms[0].R_EAR_DB_1000.disabled = obj.checked;
        }
        if (document.forms[0].R_EAR_DB_4000) {
            document.forms[0].R_EAR_DB_4000.disabled = obj.checked;
        }
        if (document.forms[0].R_EAR_DB_IN) {
            document.forms[0].R_EAR_DB_IN.disabled = obj.checked;
        }
        if (document.forms[0].R_EAR_IN) {
            document.forms[0].R_EAR_IN.disabled = obj.checked;
        }
        if (document.forms[0].R_EAR_DB_4000_IN) {
            document.forms[0].R_EAR_DB_4000_IN.disabled = obj.checked;
        }
    } else {
        if (document.forms[0].L_EAR_DB) {
            document.forms[0].L_EAR_DB.disabled = obj.checked;
        }
        if (document.forms[0].L_EAR) {
            document.forms[0].L_EAR.disabled = obj.checked;
        }
        if (document.forms[0].L_EAR_DB_1000) {
            document.forms[0].L_EAR_DB_1000.disabled = obj.checked;
        }
        if (document.forms[0].L_EAR_DB_4000) {
            document.forms[0].L_EAR_DB_4000.disabled = obj.checked;
        }
        if (document.forms[0].L_EAR_DB_IN) {
            document.forms[0].L_EAR_DB_IN.disabled = obj.checked;
        }
        if (document.forms[0].L_EAR_IN) {
            document.forms[0].L_EAR_IN.disabled = obj.checked;
        }
        if (document.forms[0].L_EAR_DB_4000_IN) {
            document.forms[0].L_EAR_DB_4000_IN.disabled = obj.checked;
        }
    }
}
//数値かどうかをチェック
function Num_Check(obj) {
    var name = obj.name;
    var checkString = obj.value;
    var newString = "";
    var count = 0;

    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i + 1);
        if ((ch >= "0" && ch <= "9") || ch == ".") {
            newString += ch;
        }
    }
    if (checkString != newString) {
        alert("{rval MSG901}\n数値を入力してください。");
        obj.value = "";
        obj.focus();
        return false;
    }
}
//視力（文字）チェック
function Mark_Check(obj) {
    var mark = obj.value;
    var printKenkouSindanIppan = document.forms[0].printKenkouSindanIppan.value;
    var msg = "A～Dを入力して下さい。";
    if (printKenkouSindanIppan == "2") {
        msg = "A～D,未を入力して下さい。";
    }
    switch (mark) {
        case "a":
        case "A":
        case "ａ":
        case "Ａ":
            obj.value = "A";
            break;
        case "b":
        case "B":
        case "ｂ":
        case "Ｂ":
            obj.value = "B";
            break;
        case "c":
        case "C":
        case "ｃ":
        case "Ｃ":
            obj.value = "C";
            break;
        case "d":
        case "D":
        case "ｄ":
        case "Ｄ":
            obj.value = "D";
            break;
        case "":
            obj.value = "";
            break;
        case "未":
            if (printKenkouSindanIppan != "2") {
                alert(msg);
                obj.value = "";
            }
            break;
        default:
            alert(msg);
            obj.value = "";
            break;
    }
}
