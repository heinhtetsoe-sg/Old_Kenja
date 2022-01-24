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

function ShowConfirm() {
    if (!confirm("{rval MSG106}")) {
        return false;
    }
}

function syokenNyuryoku(obj, target_obj, nameCd2List) {
    if (obj.value != "" && nameCd2List.indexOf(obj.value) >= 0) {
        target_obj.disabled = false;
    } else {
        if (target_obj.value) {
            alert("テキストデータは更新時に削除されます");
        }
        target_obj.disabled = true;
    }
}

function closing_window() {
    alert("{rval MSG300}");
    closeWin();
    return true;
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
            if (document.forms[0].elements[iii].name != "RCHECK17") {
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

function temp_clear() {
    ClearList(document.forms[0].left_select, document.forms[0].left_select);
    ClearList(document.forms[0].right_select, document.forms[0].right_select);
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

//その他疾病及び異常
function OptionUse(obj) {
    if (obj.value == "99") {
        document.forms[0].OTHERDISEASE.disabled = false;
        document.forms[0].OTHERDISEASE.style.backgroundColor = "#ffffff";
    } else {
        document.forms[0].OTHERDISEASE.disabled = true;
        document.forms[0].OTHERDISEASE.style.backgroundColor = "#D3D3D3";
    }
}
//歯肉の状態コンボ変更時、（熊本のみ）
function setCheckOn(obj) {
    if (obj.value == "02") {
        document.forms[0].DENTISTREMARK_GO.checked = true;
        document.getElementById("ari_nasi_go").innerHTML = "有";
    } else {
        document.forms[0].DENTISTREMARK_GO.checked = false;
        document.getElementById("ari_nasi_go").innerHTML = "無";
    }
    if (obj.value == "03") {
        document.forms[0].DENTISTREMARK_G.checked = true;
        document.getElementById("ari_nasi_g").innerHTML = "有";
    } else {
        document.forms[0].DENTISTREMARK_G.checked = false;
        document.getElementById("ari_nasi_g").innerHTML = "無";
    }
    return;
}
