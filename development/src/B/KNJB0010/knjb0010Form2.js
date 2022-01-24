function btn_submit(cmd) {
    if (document.forms[0].GROUPCD.value == "") {
        alert("{rval MSG304}");
        return true;
    } else if (cmd == "clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit() {
    if (document.forms[0].GROUPCD.value == "") {
        alert("{rval MSG304}");
        return true;
    }
    attribute3 = document.forms[0].selectdata;
    attribute4 = document.forms[0].selectdata2;
    attribute3.value = "";
    attribute4.value = "";
    sep = "";
    if (
        document.forms[0].left_chaircd.length == 0 &&
        document.forms[0].right_chaircd.length == 0
    ) {
        alert("{rval MSG916}");
        return false;
    }
    for (var i = 0; i < document.forms[0].left_chaircd.length; i++) {
        attribute3.value =
            attribute3.value +
            sep +
            document.forms[0].left_chaircd.options[i].value;
        sep = ",";
    }

    if (
        document.forms[0].left_class.length == 0 &&
        document.forms[0].right_class.length == 0
    ) {
        alert("{rval MSG916}");
        return false;
    }
    sep = "";
    for (var i = 0; i < document.forms[0].left_class.length; i++) {
        attribute4.value =
            attribute4.value +
            sep +
            document.forms[0].left_class.options[i].value;
        sep = ",";
    }

    if (attribute3.value == "") {
        if (!confirm("{rval MSG105}" + "(週授業回数,連続枠数)")) {
            return false;
        }
    }

    document.forms[0].cmd.value = "update";
    document.forms[0].submit();
    return false;
}

function move1(side, left, right, sort) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var a = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute1;
    var attribute2;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all") {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    } else {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y = current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        a[tempa[y]] = temp1[y];
    }
    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (side == "right" || side == "left") {
            if (attribute1.options[i].selected) {
                y = current1++;
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].text;
                a[tempa[y]] = temp1[y];
            } else {
                y = current2++;
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        } else {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            a[tempa[y]] = temp1[y];
        }
    }
    if (sort) {
        //sort
        tempa = tempa.sort();
        //generating new options
        for (var i = 0; i < tempa.length; i++) {
            //            alert(a[tempa[i]]);
            temp1[i] = a[tempa[i]];
        }
    }

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text = tempa[i];
    }

    //generating new options
    ClearList(attribute1);
    if (temp2.length > 0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text = tempb[i];
        }
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0][left].length; i++) {
        attribute3.value =
            attribute3.value + sep + document.forms[0][left].options[i].value;
        sep = ",";
    }
}

function move2(side, left, right, sort) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var a = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute1;
    var attribute2;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all2") {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    } else {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y = current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        a[tempa[y]] = temp1[y];
    }
    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (side == "right" || side == "left") {
            if (attribute1.options[i].selected) {
                y = current1++;
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].text;
                a[tempa[y]] = temp1[y];
            } else {
                y = current2++;
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        } else {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            a[tempa[y]] = temp1[y];
        }
    }
    if (sort) {
        //sort
        tempa = tempa.sort();
        //generating new options
        for (var i = 0; i < tempa.length; i++) {
            //            alert(a[tempa[i]]);
            temp1[i] = a[tempa[i]];
        }
    }

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text = tempa[i];
    }

    //generating new options
    ClearList(attribute1);
    if (temp2.length > 0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text = tempb[i];
        }
    }

    attribute3 = document.forms[0].selectdata2;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0][left].length; i++) {
        attribute3.value =
            attribute3.value + sep + document.forms[0][left].options[i].value;
        sep = ",";
    }
}

//Xg
function ClearList(OptionList) {
    OptionList.length = 0;
}
