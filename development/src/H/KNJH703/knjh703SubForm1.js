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

function doSubmit() {
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
    document.forms[0].cmd.value = "replace_update";
    document.forms[0].submit();
    return false;
}

//文理区分変更時、選択科目の内容を変更する
function changeSubclassBunri() {
    //現在のデータを保持する
    setHiddenData("BUNRIDIV", 1);
    setHiddenData("SUBCLASSCD", 1);
    setHiddenData("DECLINE_FLG", 2);

    //リロード
    document.forms[0].cmd.value = "replace_bunri";
    document.forms[0].submit();
    return false;
}

//hiddenに画面上のデータをセットする
function setHiddenData(name, type) {
    var element = document.getElementsByName(name);
    var val = "";
    if (type == 1) {
        val = element[0].value;
    } else {
        val = element[0].checked ? 1 : 0;
    }
    element = document.getElementsByName("H_" + name);
    element[0].value = val;
}
