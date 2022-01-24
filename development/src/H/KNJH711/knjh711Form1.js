function btn_submit(cmd) {
    if (cmd == "clear") {
        result = confirm("{rval MSG107}");
        if (result == false) {
            return false;
        }
    } else if (cmd == "update") {
        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        if (
            document.forms[0].capacity.value < document.forms[0].CATEGORY_SELECTED.length
        ) {
            alert("{rval MSG915}");
            return false;
        }
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            attribute3.value =
                attribute3.value +
                sep +
                document.forms[0].CATEGORY_SELECTED.options[i].value;
            sep = ",";
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function move1(side, student) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute;

    //教室を選択せずに生徒を移動させた場合
    if (document.forms[0].FACCD.value == "") {
        alert("生徒を割当てる場合、教室を選択して下さい。");
        return false;
    }

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y = current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        if (student) {
            tempaa[y] =
                String(attribute2.options[i].text).substring(9, 12) + "," + y;
        } else {
            tempaa[y] = attribute2.options[i].value + "," + y;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            if (student) {
                tempaa[y] =
                    String(attribute1.options[i].text).substring(9, 12) +
                    "," +
                    y;
            } else {
                tempaa[y] = attribute1.options[i].value + "," + y;
            }
        } else {
            y = current2++;
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(",");

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text = tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1, attribute1);
    if (temp2.length > 0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text = tempb[i];
        }
    }
    document.getElementById("count").innerHTML =
        document.forms[0].CATEGORY_SELECTED.length;
}

function moves(sides, student) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z = 0;

    //教室を選択せずに生徒を移動させた場合
    if (document.forms[0].FACCD.value == "") {
        alert("生徒を割当てる場合、教室を選択して下さい。");
        return false;
    }

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;
    }

    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z = current5++;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        if (student) {
            tempaa[z] =
                String(attribute6.options[i].text).substring(9, 12) + "," + z;
        } else {
            tempaa[z] = attribute6.options[i].value + "," + z;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z = current5++;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        if (student) {
            tempaa[z] =
                String(attribute5.options[i].text).substring(9, 12) + "," + z;
        } else {
            tempaa[z] = attribute5.options[i].value + "," + z;
        }
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(",");

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text = tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5, attribute5);

    //割り当て人数
    document.getElementById("count").innerHTML =
        document.forms[0].CATEGORY_SELECTED.length;
}
