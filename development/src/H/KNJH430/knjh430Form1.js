function btn_submit(cmd) {

    selectdata = document.forms[0].selectdata;
    selectdata.value = "";
    sep = "";

    for (var i = 0; i < document.forms[0].LEFT_SELECT.length; i++) {
        var val = document.forms[0].LEFT_SELECT.options[i].value.split(':');
        selectdata.value = selectdata.value + sep + val[1];
        sep = ",";
    }

    if (cmd == 'update') {
        if (document.forms[0].LEFT_SELECT.length == 0 && document.forms[0].RIGHT_SELECT.length == 0) {
            alert('{rval MSG916}');
            return false;
        }
        if (document.forms[0].LEFT_SELECT.length > 12) {
            alert('{rval MSG915}'+'\n（12項目まで）');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

/**********************/
/**  リストtoリスト  **/
/**********************/
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].RIGHT_SELECT;
    ClearList(attribute,attribute);
    attribute = document.forms[0].LEFT_SELECT;
    ClearList(attribute,attribute);
}

function move1(side) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].RIGHT_SELECT;
        attribute2 = document.forms[0].LEFT_SELECT;
    } else {
        attribute1 = document.forms[0].LEFT_SELECT;
        attribute2 = document.forms[0].RIGHT_SELECT;
    }


    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        if (side == "right") {
            var val = attribute2.options[i].value.split(':');
            tempaa[y] = val[1]+","+y;
        } else {
            tempaa[y] = attribute2.options[i].value+","+y;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            if (side == "right") {
                var val = attribute1.options[i].value.split(':')
                tempaa[y] = val[1]+","+y;
            } else {
                tempaa[y] = attribute1.options[i].value+","+y;
            }
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    if (side == "right") {
        tempaa.sort();
    }

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1,attribute1);
    if (temp2.length>0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }
}

function moves(sides) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].RIGHT_SELECT;
        attribute6 = document.forms[0].LEFT_SELECT;
    } else {
        attribute5 = document.forms[0].LEFT_SELECT;
        attribute6 = document.forms[0].RIGHT_SELECT;
    }

    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        if (sides == "right") {
            var val = attribute6.options[i].value.split(':');
            tempaa[z] = val[1]+","+z;
        } else {
            tempaa[z] = attribute6.options[i].value+","+z;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        if (sides == "right") {
            var val = attribute5.options[i].value.split(':');
            tempaa[z] = val[1]+","+z;
        } else {
            tempaa[z] = attribute5.options[i].value+","+z;
        }
    }

    if (sides == "right") {
        tempaa.sort();
    }

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5,attribute5);
}