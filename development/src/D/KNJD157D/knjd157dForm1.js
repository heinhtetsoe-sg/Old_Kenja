function btn_submit(cmd) {
    if (cmd == "csv") {
        //CSV出力時、出力対象チェック
        if (document.forms[0].CLASS_SELECTED.length == 0) {
            alert("{rval MSG916}");
            return;
        }
    }

    if (document.forms[0].CLASS_NAME) {
        for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
            document.forms[0].CLASS_NAME.options[i].selected = 0;
        }

        for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
            document.forms[0].CLASS_SELECTED.options[i].selected = 1;
            document.forms[0].SELECT.value +=
                document.forms[0].CLASS_SELECTED.options[i].value + ",";
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL, cmd) {
    document.forms[0].cmd.value = cmd;
    if (document.forms[0].CLASS_SELECTED) {
        if (document.forms[0].CLASS_SELECTED.length == 0) {
            alert("{rval MSG916}");
            return;
        }
    }

    if (document.forms[0].TESTKINDCD.value == "") {
        alert("試験が選択されていません。");
        return;
    }
    if (document.forms[0].CLASS_NAME) {
        for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
            document.forms[0].CLASS_NAME.options[i].selected = 0;
        }

        for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
            document.forms[0].CLASS_SELECTED.options[i].selected = 1;
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL + "/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function ClearList(OptionList) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
    ClearList(document.forms[0].CLASS_NAME);
    ClearList(document.forms[0].CLASS_SELECTED);
}

function cmpVal(a, b) {
    if (a.value < b.value) {
        return -1;
    } else if (a.value > b.value) {
        return 1;
    }
    return 0;
}

function move1(side) {
    var attribute1, attribute2;
    var temp1 = [];
    var temp2 = [];
    var o, i;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;
    }

    //fill an array with old values
    for (i = 0; i < attribute2.length; i++) {
        o = attribute2.options[i];
        temp1.push({ value: o.value, text: o.text });
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {
        o = attribute1.options[i];
        if (o.selected) {
            temp1.push({ value: o.value, text: o.text });
        } else {
            temp2.push({ value: o.value, text: o.text });
        }
    }

    temp1.sort(cmpVal);

    //generating new options
    for (i = 0; i < temp1.length; i++) {
        o = temp1[i];
        attribute2.options[i] = new Option();
        attribute2.options[i].value = o.value;
        attribute2.options[i].text = o.text;
    }

    //generating new options
    ClearList(attribute1);
    for (i = 0; i < temp2.length; i++) {
        o = temp2[i];
        attribute1.options[i] = new Option();
        attribute1.options[i].value = o.value;
        attribute1.options[i].text = o.text;
    }
}

function moves(sides) {
    var attribute5, attribute6;
    var temp = [];
    var i;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute5 = document.forms[0].CLASS_SELECTED;
        attribute6 = document.forms[0].CLASS_NAME;
    }

    //fill an array with old values
    for (i = 0; i < attribute6.length; i++) {
        o = attribute6.options[i];
        temp.push({ value: o.value, text: o.text });
    }

    //assign new values to arrays
    for (i = 0; i < attribute5.length; i++) {
        o = attribute5.options[i];
        temp.push({ value: o.value, text: o.text });
    }

    temp.sort(cmpVal);

    //generating new options
    for (i = 0; i < temp.length; i++) {
        o = temp[i];

        attribute6.options[i] = new Option();
        attribute6.options[i].value = o.value;
        attribute6.options[i].text = o.text;
    }

    //generating new options
    ClearList(attribute5);
}
