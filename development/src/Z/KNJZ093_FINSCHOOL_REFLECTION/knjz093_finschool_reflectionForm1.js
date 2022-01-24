function btn_submit(cmd) {

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        } else {
            cmd = "";
        }
    }

    //処理中は更新ボタンを使用不可
    document.forms[0].btn_update.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit() {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";

    if (document.forms[0].CATEGORY_SELECTED.length == 0 && document.forms[0].CATEGORY_NAME.length == 0) {
        alert('{rval MSG916}');
        return false;
    }
    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
        sep = ",";
    }

    //処理中は更新ボタンを使用不可
    document.forms[0].btn_update.disabled = true;

    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

function ClearList(OptionList) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].CATEGORY_NAME;
    ClearList(attribute);
    attribute = document.forms[0].CATEGORY_SELECTED;
    ClearList(attribute);
}

function compareByValue(o1, o2) {
    if (o1.value < o2.value) {
        return -1;
    } else if (o1.value > o2.value) {
        return 1;
    }
    return 0;
}

function move1(side) {
    var attribute1, attribute2;
    var tempa = [];
    var tempb = [];
    var i, o, len, in1, in2;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;
    }

    //fill an array with old values
    for (i = 0, len = attribute2.length; i < len; i++) {
        o = attribute2.options[i];
        tempa.push({value: o.value, text: o.text});
    }

    //assign new values to arrays
    for (i = 0, len = attribute1.length; i < len; i++) {
        o = attribute1.options[i];
        if (o.selected) {
            tempa.push({value: o.value, text: o.text});
        } else {
            tempb.push({value: o.value, text: o.text});
        }
    }
    tempa.sort(compareByValue);

    //generating new options
    in2 = "";
    for (i = 0, len = tempa.length; i < len; i++) {
        o = tempa[i];
        in2 += "<option value='" + o.value + "'>" + o.text + "</option>";
    }
    attribute2.innerHTML = in2;

    in1 = "";
    for (i = 0, len = tempb.length; i < len; i++) {
        o = tempb[i];
        in1 += "<option value='" + o.value + "'>" + o.text + "</option>";
    }
    attribute1.innerHTML = in1;
}

function moves(sides) {
    var attribute5, attribute6;
    var tempaa = [];
    var i, o, len, in6;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;
    }

    //fill an array with old values
    for (i = 0, len = attribute6.length; i < len; i++) {
        o = attribute6.options[i];
        tempaa.push({value: o.value, text: o.text});
    }

    //assign new values to arrays
    for (i = 0, len = attribute5.length; i < len; i++) {
        o = attribute5.options[i];
        tempaa.push({value: o.value, text: o.text});
    }
    tempaa.sort(compareByValue);

    //generating new options
    in6 = "";
    for (i = 0, len = tempaa.length; i < len; i++) {
        o = tempaa[i];
        in6 += "<option value='" + o.value + "'>" + o.text + "</option>";
    }
    attribute6.innerHTML = in6;
    attribute5.innerHTML = "";
}
