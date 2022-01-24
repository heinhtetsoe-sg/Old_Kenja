//サブミット
function btn_submit(cmd) {
    if (cmd == "update") {
        if (document.forms[0].student_selected.length == 0) {
            alert('生徒を選択して下さい。');
            return;
        }
        //ATTEND_PETITION_HDATに登録できるのは10個まで
        var cnt = document.forms[0].category_selected.length;
        if (document.forms[0].SONOTA.value != '') {
            cnt++;
        }
        if (cnt > 10) {
            alert('症状・理由が登録できるのは10項目までです。');
            return;
        }
    }
    attribute1 = document.forms[0].selectStudent;
    attribute1.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].student_selected.length; i++)
    {
        attribute1.value = attribute1.value + sep + document.forms[0].student_selected.options[i].value;
        sep = ",";
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].category_selected.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function move1(side, div) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;

    if (side == "left") {
        if (div == "di") {
            attribute1 = document.forms[0].category_name;
            attribute2 = document.forms[0].category_selected;
        } else {
            attribute1 = document.forms[0].student_name;
            attribute2 = document.forms[0].student_selected;
        }
    } else {
        if (div == "di") {
            attribute1 = document.forms[0].category_selected;
            attribute2 = document.forms[0].category_name;
        } else {
            attribute1 = document.forms[0].student_selected;
            attribute2 = document.forms[0].student_name;
        }
    }


    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value+","+y;
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

function moves(sides, div) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        if (div == "di") {
            attribute5 = document.forms[0].category_name;
            attribute6 = document.forms[0].category_selected;
        } else {
            attribute5 = document.forms[0].student_name;
            attribute6 = document.forms[0].student_selected;
        }
    } else {
        if (div == "di") {
            attribute5 = document.forms[0].category_selected;
            attribute6 = document.forms[0].category_name;
        } else {
            attribute5 = document.forms[0].student_selected;
            attribute6 = document.forms[0].student_name;
        }
    }

    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value+","+z;
    }

    if (sides == "right") {
        tempaa.sort();
    }

    //generating new options
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
