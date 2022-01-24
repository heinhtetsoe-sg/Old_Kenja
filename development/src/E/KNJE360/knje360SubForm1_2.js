function btn_submit(cmd) {
    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return true;
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = '';
    sep = '';
    for (var i = 0; i < document.forms[0].left_select.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ',';
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm() {
    if (!confirm('{rval MSG106}')) {
        return false;
    }
}
function check_all(obj) {
    var ii = 0;
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == 'RCHECK' + ii) {
            document.forms[0].elements[i].checked = obj.checked;
            ii++;
        }
    }
}
function doSubmit() {
    var ii = 0;
    var rcheckArray = new Array();
    var checkFlag = false;
    for (var iii = 0; iii < document.forms[0].elements.length; iii++) {
        if (document.forms[0].elements[iii].name == 'RCHECK' + ii) {
            rcheckArray.push(document.forms[0].elements[iii]);
            ii++;
        }
    }
    for (var k = 0; k < rcheckArray.length; k++) {
        if (rcheckArray[k].checked) {
            checkFlag = true;
            break;
        }
    }

    alert('{rval MSG102}');
    attribute3 = document.forms[0].selectdata;
    attribute3.value = '';
    sep = '';
    if (document.forms[0].left_select.length == 0 && document.forms[0].right_select.length == 0) {
        alert('{rval MSG916}');
        return false;
    }
    for (var i = 0; i < document.forms[0].left_select.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ',';
    }

    if (document.forms[0].ENTRYDATE.value == '') {
        alert('データを入力してください。\n　　（登録日）');
        return true;
    }
    if (document.forms[0].QUESTIONNAIRECD.value == '') {
        alert('データを選択してください。\n　　（調査名）');
        return true;
    }
    if (document.forms[0].SCHOOL_GROUP1.value == '') {
        alert('データを選択してください。\n　　（第一希望・学校系列）');
        return true;
    }

    var date = document.forms[0].ENTRYDATE.value.split('/');
    var sdate = document.forms[0].SDATE.value.split('/');
    var edate = document.forms[0].EDATE.value.split('/');
    sdate_show = document.forms[0].SDATE.value;
    edate_show = document.forms[0].EDATE.value;

    if (new Date(eval(sdate[0]), eval(sdate[1]) - 1, eval(sdate[2])) > new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2])) || new Date(eval(edate[0]), eval(edate[1]) - 1, eval(edate[2])) < new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2]))) {
        alert('登録日が入力範囲外です。\n（' + sdate_show + '～' + edate_show + '）');
        return true;
    }

    document.forms[0].cmd.value = 'replace_update1';
    document.forms[0].submit();
    return false;
}
function temp_clear() {
    ClearList(document.forms[0].left_select, document.forms[0].left_select);
    ClearList(document.forms[0].right_select, document.forms[0].right_select);
}

function ClearList(OptionList, TitleName) {
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
    var y = 0;
    var attribute;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == 'left') {
        attribute1 = document.forms[0].right_select;
        attribute2 = document.forms[0].left_select;
    } else {
        attribute1 = document.forms[0].left_select;
        attribute2 = document.forms[0].right_select;
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y = current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value + ',' + y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value + ',' + y;
        } else {
            y = current2++;
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
        attribute2.options[i].text = tempa[tmp[1]];
    }

    //generating new options
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

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == 'left') {
        attribute5 = document.forms[0].right_select;
        attribute6 = document.forms[0].left_select;
    } else {
        attribute5 = document.forms[0].left_select;
        attribute6 = document.forms[0].right_select;
    }

    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z = current5++;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value + ',' + z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z = current5++;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value + ',' + z;
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text = tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5, attribute5);
}
