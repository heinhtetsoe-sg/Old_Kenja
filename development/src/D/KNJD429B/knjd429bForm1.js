function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function retfalse() {
    return false;
}

function resetOutput1(printSide1) {
    if (printSide1.checked == false) {
        if (!document.forms[0].SCHOOL_KIND || document.forms[0].SCHOOL_KIND.value != "H") {
            document.forms[0].OUTPUT1[0].checked = false;
            document.forms[0].OUTPUT1[1].value = "";
        } else {
            document.forms[0].OUTPUT1.checked = false;
        }
    }
}

function chkPrintSide1(output1) {
    if (output1.checked == true) {
        document.forms[0].PRINT_SIDE1.checked = true;
    }
}

//異動対象日付変更
function tmp_list(cmd, submit) {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    attribute4 = document.forms[0].selectdataText;
    attribute4.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
        attribute4.value = attribute4.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].text;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    if (submit == 'on') {
        document.forms[0].submit();
        return false;
    }
}

function newwin(SERVLET_URL){
    var i;
    if (document.forms[0].CATEGORY_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return;
    }
    if (document.forms[0].DATE.value == '') {
        alert("異動対象日付が未入力です。");
        return;
    }

    // 出力ページチェック
    if (document.forms[0].PRINT_SIDE1.checked == false && 
        document.forms[0].PRINT_SIDE2.checked == false && 
        document.forms[0].PRINT_SIDE3.checked == false) {
        alert("一つ以上チェックを入れて下さい。\n(出力するページ)");
        return;
    }

    for (i = 0; i < document.forms[0].category_name.length; i++) {
        document.forms[0].category_name.options[i].selected = 0;
    }

    for (i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        document.forms[0].CATEGORY_SELECTED.options[i].selected = 1;
    }

    var bks = [];
    for (i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        bks[i] = document.forms[0].CATEGORY_SELECTED.options[i].value;
    }
    for (i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        document.forms[0].CATEGORY_SELECTED.options[i].value = document.forms[0].CATEGORY_SELECTED.options[i].value.split("-")[1];
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
    for (i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        document.forms[0].CATEGORY_SELECTED.options[i].value = bks[i];
    }
}

function ClearList(OptionList) {
    OptionList.length = 0;
}

function move1(side) {
    var temp1 = [];
    var temp2 = [];
    var tempa = [];
    var tempb = [];
    var tempaa = [];
    var y=0;
    var attribute;
    var i;
    var val, tmp;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].category_name;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].category_name;
    }


    //fill an array with old values
    for (i = 0; i < attribute2.length; i++) {
        y = temp1.length;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value + "," + y;
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y = temp1.length;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value + "," + y;
        } else {
            y = temp2.length;
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    //generating new options
    for (i = 0; i < temp1.length; i++) {
        val = tempaa[i];
        tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1);
    for (i = 0; i < temp2.length; i++) {
        attribute1.options[i] = new Option();
        attribute1.options[i].value = temp2[i];
        attribute1.options[i].text =  tempb[i];
    }
}

function moves(sides) {
    var i;
    var temp5 = [];
    var tempc = [];
    var tempaa = [];
    var z = 0;
    var val, tmp;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].category_name;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].category_name;
    }


    //fill an array with old values
    for (i = 0; i < attribute6.length; i++) {
        z = temp5.length;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value + "," + z;
    }

    //assign new values to arrays
    for (i = 0; i < attribute5.length; i++) {
        z = temp5.length;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value + "," + z;
    }

    tempaa.sort();

    //generating new options 
    for (i = 0; i < temp5.length; i++) {
        val = tempaa[i];
        tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    ClearList(attribute5);
}

