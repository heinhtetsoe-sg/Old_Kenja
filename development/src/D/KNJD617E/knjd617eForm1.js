function btn_submit(cmd) {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = '';
    attribute4 = document.forms[0].selectdata2;
    attribute4.value = '';
    sep = '';
    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
        sep = ',';
    }
    sep = '';
    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED2.length; i++) {
        attribute4.value = attribute4.value + sep + document.forms[0].CATEGORY_SELECTED2.options[i].value;
        sep = ',';
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    return false;
}

function newwin(SERVLET_URL) {
    if (document.forms[0].CATEGORY_SELECTED.length == 0) {
        alert('{rval MSG916}' + '\n出力対象者が選択されていません。');
        return false;
    }

    if (document.forms[0].EDATE.value == '') {
        alert('日付が不正です。');
        document.forms[0].EDATE.focus();
        return false;
    }

    var day = document.forms[0].EDATE.value; //印刷範囲日付
    var sdate = document.forms[0].SEME_SDATE.value; //学期開始日付
    var edate = document.forms[0].SEME_EDATE.value; //学期終了日付

    if (sdate > day || edate < day) {
        alert('日付が学期の範囲外です');
        return;
    }

    document.forms[0].DATE.value = document.forms[0].EDATE.value;

    for (var i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
        document.forms[0].CATEGORY_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        document.forms[0].CATEGORY_SELECTED.options[i].selected = 1;
    }

    for (var i = 0; i < document.forms[0].CATEGORY_NAME2.length; i++) {
        document.forms[0].CATEGORY_NAME2.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED2.length; i++) {
        document.forms[0].CATEGORY_SELECTED2.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + '/KNJD';
    document.forms[0].target = '_blank';
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

var check_checked = function () {};

window.onload = check_checked;

/****************************************** リストtoリスト ********************************************/
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].CATEGORY_NAME;
    ClearList(attribute, attribute);
    attribute = document.forms[0].CATEGORY_SELECTED;
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

    if (side == 'left') {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;
    }

    for (var i = 0; i < attribute2.length; i++) {
        y = current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].text).substring(10, 13) + ',' + y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = String(attribute1.options[i].text).substring(10, 13) + ',' + y;
        } else {
            y = current2++;
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

    if (sides == 'left') {
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;
    }

    for (var i = 0; i < attribute6.length; i++) {
        z = current5++;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = String(attribute6.options[i].text).substring(10, 13) + ',' + z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z = current5++;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = String(attribute5.options[i].text).substring(10, 13) + ',' + z;
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

function move1_2(side) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute;

    if (side == 'left') {
        attribute1 = document.forms[0].CATEGORY_NAME2;
        attribute2 = document.forms[0].CATEGORY_SELECTED2;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED2;
        attribute2 = document.forms[0].CATEGORY_NAME2;
    }

    for (var i = 0; i < attribute2.length; i++) {
        y = current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].text).substring(0, 8) + ',' + y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = String(attribute1.options[i].text).substring(0, 8) + ',' + y;
        } else {
            y = current2++;
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }
    if (document.forms[0].MAXSEL2.value - current1 < 0) {
        alert('{rval MSG915}' + '選択は' + document.forms[0].MAXSEL2.value + 'までです。');
        return;
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

function moves_2(sides) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z = 0;

    if (sides == 'left') {
        attribute5 = document.forms[0].CATEGORY_NAME2;
        attribute6 = document.forms[0].CATEGORY_SELECTED2;
        if (document.forms[0].MAXSEL2.value - attribute6.length < attribute5.length) {
            alert('{rval MSG915}' + '選択は' + document.forms[0].MAXSEL2.value + 'までです。');
            return;
        }
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED2;
        attribute6 = document.forms[0].CATEGORY_NAME2;
    }

    for (var i = 0; i < attribute6.length; i++) {
        z = current5++;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = String(attribute6.options[i].text).substring(0, 8) + ',' + z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z = current5++;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = String(attribute5.options[i].text).substring(0, 8) + ',' + z;
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