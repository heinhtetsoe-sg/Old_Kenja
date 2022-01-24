function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    //必須チェック
    if (document.forms[0].TESTDIV.value == '') {
        alert('{rval MSG310}\n( 入試区分 )');
        return;
    }
    if (document.forms[0].DESIREDIV.value == '') {
        alert('{rval MSG310}\n( 志望区分 )');
        return;
    }
    if (document.forms[0].SPORT_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return;
    }

    // List to List
    attribute3 = document.forms[0].SELECTED_DATA;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].PASS_LIST.length; i++) {
        document.forms[0].PASS_LIST.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].SPORT_SELECTED.length; i++) {
        document.forms[0].SPORT_SELECTED.options[i].selected = 1;
        attribute3.value = attribute3.value + sep + document.forms[0].SPORT_SELECTED.options[i].value;
        sep = ",";
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
function move1(side) {
    var temp1    = new Array();
    var temp2    = new Array();
    var tempa    = new Array();
    var tempb    = new Array();
    var tempaa   = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute;

    if (side == "left") {
        attribute1 = document.forms[0].PASS_LIST;
        attribute2 = document.forms[0].SPORT_SELECTED;
    } else {
        attribute1 = document.forms[0].SPORT_SELECTED;
        attribute2 = document.forms[0].PASS_LIST;  
    }

    for (var i = 0; i < attribute2.length; i++) {
        y = current1++;
        temp1[y]  = attribute2.options[i].value;
        tempa[y]  = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y = current1++
            temp1[y]  = attribute1.options[i].value;
            tempa[y]  = attribute1.options[i].text; 
            tempaa[y] = attribute1.options[i].value+","+y;
        } else {
            y = current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

//    tempaa.sort();

    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text  = tempa[tmp[1]];
    }

    ClearList(attribute1,attribute1);
    if (temp2.length>0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text  = tempb[i];
        }
    }
}

function moves(sides) {
    var temp5  = new Array();
    var tempc  = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z = 0;

    if (sides == "left") {
        attribute5 = document.forms[0].PASS_LIST;
        attribute6 = document.forms[0].SPORT_SELECTED;
    } else {
        attribute5 = document.forms[0].SPORT_SELECTED;
        attribute6 = document.forms[0].PASS_LIST;  
    }

    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z]  = attribute6.options[i].value;
        tempc[z]  = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z]  = attribute5.options[i].value;
        tempc[z]  = attribute5.options[i].text; 
        tempaa[z] = attribute5.options[i].value+","+z;
    }

    tempaa.sort();

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text  = tempc[tmp[1]];
    }

    ClearList(attribute5,attribute5);

}
