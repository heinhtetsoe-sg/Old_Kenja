function btn_submit(cmd) {

    document.forms[0].encoding = "multipart/form-data";

    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }

    if (document.forms[0].OUTPUT[0].checked == true || document.forms[0].OUTPUT[1].checked == true) {
        var semesters = document.forms[0].SEMESTER;
        document.forms[0].SEMESTER_GAKKI.value = semesters.options[semesters.selectedIndex].text;
    }

    if (document.forms[0].OUTPUT[3].checked == true) {
        var yearsemes = document.forms[0].YEAR_SEMES;
        document.forms[0].NENDO_GAKKI.value = yearsemes.options[yearsemes.selectedIndex].text;
    }

    if (document.forms[0].OUTPUT[1].checked == true) {
        if (document.forms[0].SHORI_MEI.value == '2' && !confirm('（再確認）削除を開始します。よろしいでしょうか？')) {
            return true;
        }
    } else if (cmd != ""){
        cmd = "csv";
    }

    if ( !document.forms[0].OUTPUT[2].checked && (cmd == 'csv' || cmd == 'exec')) {
        setCmbDataToHidden("TESTKIND_ITEMCD");
        setCmbDataToHidden("TRGTGRADE");
        setCmbDataToHidden("CHAIRCD");

        setCmbDataToHidden2();
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_submit2(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function getElementByName(eleName) {
    for (var e = 0; e < document.forms[0].elements.length; e++) {
        
        if (document.forms[0].elements[e].name == eleName) {
            return document.forms[0].elements[e];
        }
    }
    return null;
}

function getOutputCheckedVal() {
    var checkedNum = 0;
    for (var i = 0; i < document.forms[0].OUTPUT.length; i++) {
        if (document.forms[0].OUTPUT[i].checked) {
            checkedNum = i;
        }
    }
    return checkedNum;
}

function setCmbDataToHidden(cmbName) {
    var index = "";
    if (cmbName === "TESTKIND_ITEMCD") {
        var index = getOutputCheckedVal() + 1;
    }
    var ele = getElementByName(cmbName + index);
    var options = ele.options;

    var selectedIndex = options.selectedIndex;
    if (!options[selectedIndex].value == "") { //コンボの選択値が「全て出力」以外の場合
        var ele = getElementByName("HID_" + cmbName);
        ele.value = "";
        return;
    }

    var sep = "";
    var setData = "";
    for (var i = 1; i < options.length; i++) {
        setData += sep + options[i].value;
        sep = ",";
    }
        var ele = getElementByName("HID_" + cmbName);
        ele.value = setData;
}

function setCmbDataToHidden2() { //ラベル格納用
    var index = getOutputCheckedVal() + 1;
    var ele = getElementByName("TESTKIND_ITEMCD" + index);
    var options = ele.options;

    if (!options[0].value == "") return;  //コンボの選択値が「全て出力」以外ならreturn

    var sep = "";
    var setData = "";
    for (var i = 1; i < options.length; i++) {
        setData += sep + options[i].value + ":" + options[i].label;
        sep = ",";
    }
    var ele = getElementByName("HID_TESTKIND_ITEMCD_LABEL");
    ele.value = setData;
}


//セキュリティーチェック
function OnSecurityError()
{
    alert('{rval MSG300}' + '\n高セキュリティー設定がされています。');
    closeWin();
}

