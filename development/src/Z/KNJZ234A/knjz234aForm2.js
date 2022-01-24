function btn_submit(cmd) {
    if (cmd == "clear") {
        if (!confirm("{rval MSG106}")) return false;
    }

    if (cmd == "delete") {
        if (!confirm("{rval MSG103}")) return false;
    }

    if (cmd == "list") {
        window.open("knjz234aindex.php?cmd=sel&init=1", "right_frame");
    }

    if (cmd == "cdchange") {
        document.forms[0].CHAIR_GROUP_CD.value = toInteger(document.forms[0].CHAIR_GROUP_CD.value);
        cmd = "sel";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit(cmd) {
    attribute = document.forms[0].chairselect;
    attribute.value = "";
    sep = "";
    if (document.forms[0].LCHAIR.length == 0) {
        alert("{rval MSG916}");
        return false;
    }
    for (var i = 0; i < document.forms[0].LCHAIR.length; i++) {
        attribute.value = attribute.value + sep + document.forms[0].LCHAIR.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function move(side, name) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute;

    if (side == "left") {
        attribute1 = document.forms[0].RCHAIR;
        attribute2 = document.forms[0].LCHAIR;
    } else {
        attribute1 = document.forms[0].LCHAIR;
        attribute2 = document.forms[0].RCHAIR;
    }

    for (var i = 0; i < attribute2.length; i++) {
        y = current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].value) + "," + y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = String(attribute1.options[i].value) + "," + y;
        } else {
            y = current2++;
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(",");

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text = tempa[tmp[1]];
    }

    ClearList(attribute1, attribute1);
    if (temp2.length > 0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text = tempb[i];
        }
    }
}

function OnAuthError() {
    alert("{rval MZ0026}");
    closeWin();
}

function moves(side, name) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z = 0;

    if (side == "sel_add_all") {
        attribute5 = document.forms[0].RCHAIR;
        attribute6 = document.forms[0].LCHAIR;
    } else {
        attribute5 = document.forms[0].LCHAIR;
        attribute6 = document.forms[0].RCHAIR;
    }

    for (var i = 0; i < attribute6.length; i++) {
        z = current5++;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = String(attribute6.options[i].value) + "," + z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z = current5++;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = String(attribute5.options[i].value) + "," + z;
    }

    tempaa.sort();

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(",");

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text = tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5, attribute5);
}
