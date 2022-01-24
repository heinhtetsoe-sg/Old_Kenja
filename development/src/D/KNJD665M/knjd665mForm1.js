function btn_submit(cmd) {
    var attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    var attribute4 = document.forms[0].selectdata2;
    attribute4.value = "";
    var sep = "";
    var sep2 = "";
    for (var cnt = 0; cnt < document.forms[0].CATEGORY_NAME.length; cnt++) {
        document.forms[0].CATEGORY_NAME.options[cnt].selected = 0;
    }
    for (var cnt = 0; cnt < document.forms[0].CATEGORY_NAME2.length; cnt++) {
        document.forms[0].CATEGORY_NAME2.options[cnt].selected = 0;
    }

    for (var cnt = 0; cnt < document.forms[0].CATEGORY_SELECTED.length; cnt++) {
        document.forms[0].CATEGORY_SELECTED.options[cnt].selected = 1;
        attribute3.value = attribute3.value + sep + document.forms[0].CATEGORY_SELECTED.options[cnt].value;
        sep = ",";
    }
    for (var cnt = 0; cnt < document.forms[0].CATEGORY_SELECTED2.length; cnt++) {
        document.forms[0].CATEGORY_SELECTED2.options[cnt].selected = 1;
        attribute4.value = attribute4.value + sep2 + document.forms[0].CATEGORY_SELECTED2.options[cnt].value;
        sep2 = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    if (document.forms[0].CATEGORY_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return false;
    }

    for (var cnt = 0; cnt < document.forms[0].CATEGORY_NAME.length; cnt++) {
        document.forms[0].CATEGORY_NAME.options[cnt].selected = 0;
    }

    for (var cnt = 0; cnt < document.forms[0].CATEGORY_SELECTED.length; cnt++) {
        document.forms[0].CATEGORY_SELECTED.options[cnt].selected = 1;
    }

    for (var cnt = 0; cnt < document.forms[0].CATEGORY_NAME2.length; cnt++) {
        document.forms[0].CATEGORY_NAME2.options[cnt].selected = 0;
    }

    for (var cnt = 0; cnt < document.forms[0].CATEGORY_SELECTED2.length; cnt++) {
        document.forms[0].CATEGORY_SELECTED2.options[cnt].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

/****************************************** リストtoリスト ********************************************/
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].CATEGORY_NAME;
    ClearList(attribute,attribute);
    attribute = document.forms[0].CATEGORY_SELECTED;
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
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;
    }

    for (var cnt = 0; cnt < attribute2.length; cnt++) {
        y=current1++;
        temp1[y] = attribute2.options[cnt].value;
        tempa[y] = attribute2.options[cnt].text;
        tempaa[y] = String(attribute2.options[cnt].text).substring(10,13)+","+y;
    }

    for (var cnt = 0; cnt < attribute1.length; cnt++) {
        if ( attribute1.options[cnt].selected ) {
            y=current1++;
            temp1[y] = attribute1.options[cnt].value;
            tempa[y] = attribute1.options[cnt].text;
            tempaa[y] = String(attribute1.options[cnt].text).substring(10,13)+","+y;
        } else {
            y=current2++;
            temp2[y] = attribute1.options[cnt].value;
            tempb[y] = attribute1.options[cnt].text;
        }
    }

    tempaa.sort();

    for (var cnt = 0; cnt < temp1.length; cnt++) {
        var val = tempaa[cnt];
        var tmp = val.split(',');

        attribute2.options[cnt] = new Option();
        attribute2.options[cnt].value = temp1[tmp[1]];
        attribute2.options[cnt].text =  tempa[tmp[1]];
    }

    ClearList(attribute1,attribute1);
    if (temp2.length>0) {
        for (var cnt = 0; cnt < temp2.length; cnt++) {
            attribute1.options[cnt] = new Option();
            attribute1.options[cnt].value = temp2[cnt];
            attribute1.options[cnt].text =  tempb[cnt];
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
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;
    }

    for (var cnt = 0; cnt < attribute6.length; cnt++) {
        z=current5++;
        temp5[z] = attribute6.options[cnt].value;
        tempc[z] = attribute6.options[cnt].text;
        tempaa[z] = String(attribute6.options[cnt].text).substring(10,13)+","+z;
    }

    for (var cnt = 0; cnt < attribute5.length; cnt++) {
        z=current5++;
        temp5[z] = attribute5.options[cnt].value;
        tempc[z] = attribute5.options[cnt].text;
        tempaa[z] = String(attribute5.options[cnt].text).substring(10,13)+","+z;
    }

    tempaa.sort();

    for (var cnt = 0; cnt < temp5.length; cnt++) {
        var val = tempaa[cnt];
        var tmp = val.split(',');

        attribute6.options[cnt] = new Option();
        attribute6.options[cnt].value = temp5[tmp[1]];
        attribute6.options[cnt].text =  tempc[tmp[1]];
    }

    ClearList(attribute5,attribute5);
}

function move1_2(side) {
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
        attribute1 = document.forms[0].CATEGORY_NAME2;
        attribute2 = document.forms[0].CATEGORY_SELECTED2;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED2;
        attribute2 = document.forms[0].CATEGORY_NAME2;
    }

    for (var cnt = 0; cnt < attribute2.length; cnt++) {
        y=current1++;
        temp1[y] = attribute2.options[cnt].value;
        tempa[y] = attribute2.options[cnt].text;
        tempaa[y] = String(attribute2.options[cnt].text).substring(0,8)+","+y;
    }

    for (var cnt = 0; cnt < attribute1.length; cnt++) {
        if ( attribute1.options[cnt].selected ) {
            y=current1++;
            temp1[y] = attribute1.options[cnt].value;
            tempa[y] = attribute1.options[cnt].text;
            tempaa[y] = String(attribute1.options[cnt].text).substring(0,8)+","+y;
        } else {
            y=current2++;
            temp2[y] = attribute1.options[cnt].value;
            tempb[y] = attribute1.options[cnt].text;
        }
    }

    tempaa.sort();

    for (var cnt = 0; cnt < temp1.length; cnt++) {
        var val = tempaa[cnt];
        var tmp = val.split(',');

        attribute2.options[cnt] = new Option();
        attribute2.options[cnt].value = temp1[tmp[1]];
        attribute2.options[cnt].text =  tempa[tmp[1]];
    }

    ClearList(attribute1,attribute1);
    if (temp2.length>0) {
        for (var cnt = 0; cnt < temp2.length; cnt++) {
            attribute1.options[cnt] = new Option();
            attribute1.options[cnt].value = temp2[cnt];
            attribute1.options[cnt].text =  tempb[cnt];
        }
    }
}

function moves_2(sides) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;

    if (sides == "left") {
        attribute5 = document.forms[0].CATEGORY_NAME2;
        attribute6 = document.forms[0].CATEGORY_SELECTED2;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED2;
        attribute6 = document.forms[0].CATEGORY_NAME2;
    }

    for (var cnt = 0; cnt < attribute6.length; cnt++) {
        z=current5++;
        temp5[z] = attribute6.options[cnt].value;
        tempc[z] = attribute6.options[cnt].text;
        tempaa[z] = String(attribute6.options[cnt].text).substring(0,8)+","+z;
    }

    for (var cnt = 0; cnt < attribute5.length; cnt++) {
        z=current5++;
        temp5[z] = attribute5.options[cnt].value;
        tempc[z] = attribute5.options[cnt].text;
        tempaa[z] = String(attribute5.options[cnt].text).substring(0,8)+","+z;
    }

    tempaa.sort();

    for (var cnt = 0; cnt < temp5.length; cnt++) {
        var val = tempaa[cnt];
        var tmp = val.split(',');

        attribute6.options[cnt] = new Option();
        attribute6.options[cnt].value = temp5[tmp[1]];
        attribute6.options[cnt].text =  tempc[tmp[1]];
    }

    ClearList(attribute5,attribute5);
}
