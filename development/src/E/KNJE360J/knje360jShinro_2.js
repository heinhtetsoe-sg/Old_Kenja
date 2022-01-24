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
function check_all(obj) {
    var ii = 0;
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "RCHECK" + ii) {
            document.forms[0].elements[i].checked = obj.checked;
            ii++;
        }
    }
}
function doSubmit() {
    var ii = 0;
    var rcheckArray = new Array();
    var checkFlag = false;
    for (var iii = 0; iii < document.forms[0].elements.length; iii++) {
        if (document.forms[0].elements[iii].name == "RCHECK" + ii) {
            rcheckArray.push(document.forms[0].elements[iii]);
            ii++;
        }
    }
    for (var k = 0; k < rcheckArray.length; k++) {
        if (rcheckArray[k].checked) {
            checkFlag = true;
            break;
        }
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

    if (document.forms[0].ENTRYDATE.value == "") {
        alert("データを入力してください。\n　　（登録日）");
        return true;
    }
    if (document.forms[0].QUESTIONNAIRECD.value == "") {
        alert("データを選択してください。\n　　（調査名）");
        return true;
    }
    if (document.forms[0].SCHOOL_GROUP1.value == "") {
        alert("データを選択してください。\n　　（第一希望・学校系列）");
        return true;
    }

    var date = document.forms[0].ENTRYDATE.value.split("/");
    var sdate = document.forms[0].SDATE.value.split("/");
    var edate = document.forms[0].EDATE.value.split("/");
    sdate_show = document.forms[0].SDATE.value;
    edate_show = document.forms[0].EDATE.value;

    if (
        new Date(eval(sdate[0]), eval(sdate[1]) - 1, eval(sdate[2])) > new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2])) ||
        new Date(eval(edate[0]), eval(edate[1]) - 1, eval(edate[2])) < new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2]))
    ) {
        alert("登録日が入力範囲外です。\n（" + sdate_show + "～" + edate_show + "）");
        return true;
    }

    document.forms[0].cmd.value = "replace_update1";
    document.forms[0].submit();
    return false;
}
function temp_clear() {
    ClearList(document.forms[0].left_select, document.forms[0].left_select);
    ClearList(document.forms[0].right_select, document.forms[0].right_select);
}
