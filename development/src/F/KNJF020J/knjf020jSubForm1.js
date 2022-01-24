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

function check_all(obj) {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.substring(0, 6) == "RCHECK") {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

function doSubmit() {
    var rcheckArray = new Array();
    var checkFlag = false;
    for (var iii = 0; iii < document.forms[0].elements.length; iii++) {
        if (document.forms[0].elements[iii].name.substring(0, 6) == "RCHECK") {
            if (document.forms[0].elements[iii].name != "RCHECK23") {
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
    document.forms[0].cmd.value = "replace_update";
    document.forms[0].submit();
    return false;
}

//チェックボックスのラベル表示（有・無）
function checkAri_Nasi(obj, id) {
    var ari_nasi = document.getElementById(id);
    if (obj.checked) {
        ari_nasi.innerHTML = "有";
    } else {
        ari_nasi.innerHTML = "無";
    }
}

//コンボの変更イベント
function OptionUse(obj, target_obj, disableCodes) {
    //その他疾病及び異常
    textDisabledChange(target_obj, disableCodes, obj.value);
}

function OptionUse2(obj, target_obj, disableCodes) {
    //口腔の疾病及び異常
    textDisabledChange(target_obj, disableCodes, obj.value);
}

function OptionUse3(disableCodes) {
    var target_obj1 = document.forms[0]["DENTISTREMARKCD"];
    var target_text1 = document.forms[0]["DENTISTREMARK"];
    var target_obj2 = document.forms[0]["DENTISTREMARKCD2"];
    var target_text2 = document.forms[0]["DENTISTREMARK2"];
    var target_obj3 = document.forms[0]["DENTISTREMARKCD3"];
    var target_text3 = document.forms[0]["DENTISTREMARK3"];

    //所見1
    if (target_obj1.value != "" && target_obj1.value != "00") {
        //所見1テキストの有効無効変更
        textDisabledChange(target_text1, disableCodes, target_obj1.value);
        objDisabledChange(target_obj2, false); //所見2のコンボの有効化
        if (target_obj2.value != "" && target_obj2.value != "00") {
            objDisabledChange(target_obj3, false); //所見3のコンボの有効化
            //所見2テキストの有効無効変更
            textDisabledChange(target_text2, disableCodes, target_obj2.value);
            //所見3テキストの有効無効変更
            textDisabledChange(target_text3, disableCodes, target_obj3.value);
        } else {
            objDisabledChange(target_text2, true);
            objDisabledChange(target_obj3, true);
            objDisabledChange(target_text3, true);
        }
    } else {
        objDisabledChange(target_text1, true);
        objDisabledChange(target_obj2, true);
        objDisabledChange(target_text2, true);
        objDisabledChange(target_obj3, true);
        objDisabledChange(target_text3, true);
    }
}

function OptionUse4(disableCodes) {
    var target_obj1 = document.forms[0]["DENTISTTREATCD"];
    var target_text1 = document.forms[0]["DENTISTTREAT"];
    var target_obj2 = document.forms[0]["DENTISTTREATCD2"];
    var target_text2 = document.forms[0]["DENTISTTREAT2_1"];
    //事後措置1
    if (target_obj1.value != "" && target_obj1.value != "01") {
        //事後措置1テキストの有効無効変更
        textDisabledChange(target_text1, disableCodes, target_obj1.value);
        objDisabledChange(target_obj2, false); //事後措置2のコンボの有効化
        textDisabledChange(target_text2, disableCodes, target_obj2.value);
    } else {
        objDisabledChange(target_text1, true);
        objDisabledChange(target_obj2, true);
        objDisabledChange(target_text2, true);
    }
}

function objDisabledChange(obj, disabled) {
    obj.disabled = disabled;
    if (disabled) {
        obj.style.backgroundColor = "darkgray";
    } else {
        obj.style.backgroundColor = "#ffffff";
    }
}

//所見欄の有効無効変更
function textDisabledChange(target_obj, disableCodes, targetCode) {
    if (isTextBoxDisabled(disableCodes, targetCode) == true) {
        objDisabledChange(target_obj, true);
    } else {
        objDisabledChange(target_obj, false);
    }
}

//所見欄の有効無効チェック
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
