function btn_submit(cmd) {
    if (cmd == "delete" && !confirm("{rval MSG103}")) {
        return true;
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].left_select.length; i++) {
        attribute3.value =
            attribute3.value +
            sep +
            document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
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
            } else {
                fieldName.readOnly = true;
            }
        }
    }

    return false;
}

function syokenNyuryoku(obj, target_obj) {
    if (obj.value == "") {
        var select_no = 0;
    } else {
        var select_no = parseInt(obj.value.replace(/^0+/, ""));
    }

    if (select_no < 2) {
        if (target_obj.value) {
            alert("テキストデータは更新時に削除されます");
        }
        target_obj.disabled = true;
    } else {
        target_obj.disabled = false;
    }
}

function disableChange(obj, flgObjName, target_obj) {
    var flg_obj = document.forms[0][flgObjName + obj.value];
    if (flg_obj === undefined) {
        target_obj.disabled = true;
        return;
    }

    if (flg_obj.value == "") {
        target_obj.disabled = true;
        if (target_obj.value) {
            alert("テキストデータは更新時に削除されます");
        }
    } else {
        target_obj.disabled = false;
    }
}

function ShowConfirm() {
    if (!confirm("{rval MSG106}")) {
        return false;
    }
}

function check_all(obj) {
    var ii = 1;
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.substr(0, 6) == "RCHECK") {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

function doSubmit() {
    var ii = 1;
    var rcheckArray = new Array();
    var checkFlag = false;
    for (var iii = 0; iii < document.forms[0].elements.length; iii++) {
        if (document.forms[0].elements[iii].name.substr(0, 6) == "RCHECK") {
            if (
                document.forms[0].elements[iii].name !=
                document.forms[0].CHECK_ALL.value
            ) {
                rcheckArray.push(document.forms[0].elements[iii]);
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
    if (
        document.forms[0].left_select.length == 0 &&
        document.forms[0].right_select.length == 0
    ) {
        alert("{rval MSG916}");
        return false;
    }
    for (var i = 0; i < document.forms[0].left_select.length; i++) {
        attribute3.value =
            attribute3.value +
            sep +
            document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = "replace_update2";
    document.forms[0].submit();
    return false;
}

function temp_clear() {
    ClearList(document.forms[0].left_select, document.forms[0].left_select);
    ClearList(document.forms[0].right_select, document.forms[0].right_select);
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

function syokenNyuryoku(obj, target_obj, disaleCode) {
    if (isTextBoxDisabled(disaleCode, obj.value) == true) {
        target_obj.disabled = "true";
    } else {
        target_obj.disabled = "";
    }
}

function syokenNyuryoku2(
    obj,
    target_obj,
    disaleCode,
    text_obj,
    disaleCode2,
    combo_obj
) {
    var arrTarget_objs = target_obj.split(",");
    var arrText_objs = text_obj.split(",");
    var arrCombo_objs = combo_obj.split(",");
    var targetLen = arrTarget_objs.length;
    var textLen = arrText_objs.length;
    if (isTextBoxDisabled(disaleCode, obj.value) == true) {
        for (var i = 0; i < targetLen; i++) {
            document.forms[0][arrTarget_objs[i]].disabled = "true";
        }
    } else {
        for (var i = 0; i < targetLen; i++) {
            document.forms[0][arrTarget_objs[i]].disabled = "";
        }
        for (var i = 0; i < textLen; i++) {
            if (
                isTextBoxDisabled(
                    disaleCode2,
                    document.forms[0][arrCombo_objs[i]].value
                ) == true
            ) {
                document.forms[0][arrText_objs[i]].disabled = "true";
            } else {
                document.forms[0][arrText_objs[i]].disabled = "";
            }
        }
    }
}

function isTextBoxDisabled(disaleCodes, targetCode) {
    //未選択の時は無効
    if (targetCode == "") {
        return true;
    }
    var arrDisaleCodes = disaleCodes.split(",");
    for (var i = 0; i < arrDisaleCodes.length; i++) {
        if (arrDisaleCodes[i] == targetCode) {
            return true;
        }
    }
    return false;
}
