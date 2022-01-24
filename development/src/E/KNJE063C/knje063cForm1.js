function btn_submit(cmd) {

    var attribute1 = document.forms[0].selectdata;
    var attribute2 = document.forms[0].selectdataText;
    if (cmd == "update") {
        if (document.forms[0].CATEGORY_SELECTED.length == 0) {
            alert('{rval MSG916}');
            return;
        }

        var sep = "";
        attribute1.value = "";
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            attribute1.value = attribute1.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
            attribute2.value = attribute2.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].label;
            sep = ",";
        }
    } else {
        attribute1.value = "";
        attribute2.value = "";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList) {
    OptionList.length = 0;
}

function move1(side) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var attribute1;
    var attribute2;
    var y=0;

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
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].text).substring(9,12)+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = String(attribute1.options[i].text).substring(9,12)+","+y;
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1);
    if (temp2.length > 0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }
}

function moves(sides) {
    var temp1 = new Array();
    var tempa = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var attribute1;
    var attribute2;
    var y=0;


    //assign what select attribute treat as attribute1 and attribute2
    if (sides == "left") {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].text).substring(9,12)+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        y=current1++
        temp1[y] = attribute1.options[i].value;
        tempa[y] = attribute1.options[i].text;
        tempaa[y] = String(attribute1.options[i].text).substring(9,12)+","+y;
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1);
}
