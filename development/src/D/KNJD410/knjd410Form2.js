function btn_submit(cmd) {   
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) return true;
    }
    //サブミット時、一旦、左リストをクリア
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    attribute4 = document.forms[0].selectdataLabel;
    attribute4.value = "";
    attribute5 = document.forms[0].selectschno;
    attribute5.value = "";
    //右クラス変更と更新時、左リストを保持
    if (cmd == 'change_hr_class' || cmd == 'update') {
        sep = "";
        for (var i = 0; i < document.forms[0].LEFT_PART.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].LEFT_PART.options[i].value;
            attribute4.value = attribute4.value + sep + document.forms[0].LEFT_PART.options[i].text;
            tmp = document.forms[0].LEFT_PART.options[i].value.split("-");
            attribute5.value = attribute5.value + sep + tmp[2];
            sep = ",";
        }

        with(document.forms[0]){
            document.getElementById("RIGHT_NUM").innerHTML = LEFT_PART.options.length;
            document.getElementById("LEFT_NUM").innerHTML = RIGHT_PART.options.length;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

/****************************************** リストtoリスト ********************************************/
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].RIGHT_PART;
    ClearList(attribute,attribute);
    attribute = document.forms[0].LEFT_PART;
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

    if (side == "left") {
        attribute1 = document.forms[0].RIGHT_PART;
        attribute2 = document.forms[0].LEFT_PART;
    } else {
        attribute1 = document.forms[0].LEFT_PART;
        attribute2 = document.forms[0].RIGHT_PART;
    }

    for (var i = 0; i < attribute2.length; i++) {
        y=current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value+","+y;
        } else {
            y=current2++;
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

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

    if (sides == "left") {
        attribute5 = document.forms[0].RIGHT_PART;
        attribute6 = document.forms[0].LEFT_PART;
    } else {
        attribute5 = document.forms[0].LEFT_PART;
        attribute6 = document.forms[0].RIGHT_PART;
    }

    for (var i = 0; i < attribute6.length; i++) {
        z=current5++;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z=current5++;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value+","+z;
    }

    tempaa.sort();

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    ClearList(attribute5,attribute5);
}
