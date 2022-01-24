function btn_submit(cmd) {
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
}

function doSubmit() {
    var attribute1 = document.forms[0].selectdata;
    attribute1.value = "";
    var attribute2 = document.forms[0].selectdata2;
    attribute2.value = "";

    if (document.forms[0].SCHOOL_KIND.value == "") {
        alert('{rval MSG301}' + '\n※メニュー校種');
        return false;
    }

    if (document.forms[0].SCHOOL_KIND.value != "") {
        if (document.forms[0].prgId_input.length==0 || document.forms[0].selKind_input.length==0) {
            alert('{rval MSG916}');
            return false;
        }
    }
    sep = "";
    for (var i = 0; i < document.forms[0].prgId_input.length; i++) {
        attribute1.value = attribute1.value + sep + document.forms[0].prgId_input.options[i].value;
        sep = ",";
    }
    sep = "";
    for (var i = 0; i < document.forms[0].selKind_input.length; i++) {
        attribute2.value = attribute2.value + sep + document.forms[0].selKind_input.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function move1(side) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all") {
        attribute1 = document.forms[0].selKind_input;
        attribute2 = document.forms[0].selKind_delete;
    } else {
        attribute1 = document.forms[0].selKind_delete;
        attribute2 = document.forms[0].selKind_input;
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].value + attribute2.options[i].text;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (side == "right" || side == "left") {
            if ( attribute1.options[i].selected ) {
                y=current1++
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].value + attribute1.options[i].text;
            } else {
                y=current2++
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].value + attribute1.options[i].text;
            }
        } else {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].value + attribute1.options[i].text;
        }
    }

    //sort
    temp1 = temp1.sort();
    tempa = tempa.sort();

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text = String(tempa[i]).substr(1);
    }

    //generating new options
    ClearList(attribute1,attribute1);
    if (temp2.length>0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  String(tempb[i]).substr(1);
        }
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].selKind_input.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].selKind_input.options[i].value;
        sep = ",";
    }
}

function move2(side) {
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
        attribute1 = document.forms[0].selKind_delete;
        attribute2 = document.forms[0].selKind_input;
    } else {
        attribute1 = document.forms[0].selKind_input;
        attribute2 = document.forms[0].selKind_delete;  
    }

    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

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

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
