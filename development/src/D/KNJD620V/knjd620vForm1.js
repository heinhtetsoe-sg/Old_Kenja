function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError() {
    alert('{rval MSG305}' + '\n考査毎の欠時数の集計月が未設定です。\n（名称マスタ：Z004の予備１・２）');
    closeWin();
}
// 印刷
function newwin(SERVLET_URL) {

    if (document.forms[0].category_selected.length == 0) {
        alert('{rval MSG916}');
        return;
    }
    for (var i = 0; i < document.forms[0].category_name.length; i++) {
        document.forms[0].category_name.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].category_selected.length; i++) {
        document.forms[0].category_selected.options[i].selected = 1;
    }
    if (!document.forms[0].SDATE.value) {
        alert('出欠集計開始日付が未設定です。');
        document.forms[0].SDATE.forcus();
        return;
    }
    if (!document.forms[0].DATE.value) {
        alert('出欠集計終了日付が未設定です。');
        document.forms[0].DATE.forcus();
        return;
    }
    if (document.forms[0].YEAR_SDATE.value > document.forms[0].SDATE.value) {
        alert('出欠集計開始日付が年度の範囲外です。');
        return;
    } else if (document.forms[0].YEAR_EDATE.value < document.forms[0].DATE.value) {
        alert('出欠集計終了日付が年度の範囲外です。');
        return;
    }
    if (document.forms[0].SDATE.value > document.forms[0].DATE.value) {
        alert('出欠集計日付が不正です。' + document.forms[0].SDATE.value + " // " + document.forms[0].DATE.value);
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    //        document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].category_name;
    ClearList(attribute, attribute);
    attribute = document.forms[0].category_selected;
    ClearList(attribute, attribute);
}

function move1(side) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute;

    if (side == "left") {
        attribute1 = document.forms[0].category_name;
        attribute2 = document.forms[0].category_selected;
    } else {
        attribute1 = document.forms[0].category_selected;
        attribute2 = document.forms[0].category_name;
    }


    for (var i = 0; i < attribute2.length; i++) {
        y = current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value + "," + y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value + "," + y;
        } else {
            y = current2++
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
        attribute2.options[i].text = tempa[tmp[1]];
    }

    ClearList(attribute1, attribute1);
    if (temp2.length > 0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text = tempb[i];
        }
    }
}

function moves(sides) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z = 0;

    if (sides == "left") {
        attribute5 = document.forms[0].category_name;
        attribute6 = document.forms[0].category_selected;
    } else {
        attribute5 = document.forms[0].category_selected;
        attribute6 = document.forms[0].category_name;
    }

    for (var i = 0; i < attribute6.length; i++) {
        z = current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value + "," + z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z = current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value + "," + z;
    }

    tempaa.sort();

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text = tempc[tmp[1]];
    }

    ClearList(attribute5, attribute5);

}
