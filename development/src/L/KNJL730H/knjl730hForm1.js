function btn_submit(cmd) {
    if (cmd == 'halladd'){
        if (document.forms[0].CHANGE_FLG.value == '1') {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }

        //会場追加
        loadwindow('knjl730hindex.php?cmd=halladd&APPLICANTDIV='+document.forms[0].APPLICANTDIV.value+'&TESTDIV='+document.forms[0].TESTDIV.value,300,300,450,250)
        return true;
    }

    //一覧(左側)
    selectLeft = document.forms[0].selectLeft;
    selectLeft.value = '';
    selectLeftText = document.forms[0].selectLeftText;
    selectLeftText.value = '';

    //一覧(右側)
    selectRight = document.forms[0].selectRight;
    selectRight.value = '';
    selectRightText = document.forms[0].selectRightText;
    selectRightText.value = '';

    if (cmd == 'update') {
        if (document.forms[0].EXAMHALLCD.value == '') {
            alert('{rval MSG301}' + '( 会場名 )');
            return true;
        }

        //一覧(左側)
        sep = '';
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            selectLeft.value = selectLeft.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
            selectLeftText.value = selectLeftText.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].text;
            sep = ',';
        }

        //一覧(右側)
        sep = '';
        for (var i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
            selectRight.value = selectRight.value + sep + document.forms[0].CATEGORY_NAME.options[i].value;
            selectRightText.value = selectRightText.value + sep + document.forms[0].CATEGORY_NAME.options[i].text;
            sep = ',';
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function changeValue() {
    document.forms[0].CHANGE_FLG.value = "1";
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
    var y = 0;
    var attribute;
    var i;
    var val, tmp;

    changeValue();

    //assign what select attribute treat as attribute1 and attribute2
    if (side == 'left') {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;
    }

    //fill an array with old values
    for (i = 0; i < attribute2.length; i++) {
        y = temp1.length;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value + ',' + y;
        console.log('#' + tempaa[y] + '#');
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = temp1.length;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value + ',' + y;
            console.log('#' + tempaa[y] + '#');
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
        attribute2.options[i].text = tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1);
    for (i = 0; i < temp2.length; i++) {
        attribute1.options[i] = new Option();
        attribute1.options[i].value = temp2[i];
        attribute1.options[i].text = tempb[i];
    }
}

function moves(sides) {
    var i;
    var temp5 = [];
    var tempc = [];
    var tempaa = [];
    var z = 0;
    var val, tmp;

    changeValue();

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == 'left') {
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;
    }

    //fill an array with old values
    for (i = 0; i < attribute6.length; i++) {
        z = temp5.length;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value + ',' + z;
        console.log('#' + tempaa[z] + '#');
    }

    //assign new values to arrays
    for (i = 0; i < attribute5.length; i++) {
        z = temp5.length;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value + ',' + z;
        console.log('#' + tempaa[z] + '#');
    }

    tempaa.sort();

    //generating new options
    for (i = 0; i < temp5.length; i++) {
        val = tempaa[i];
        tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text = tempc[tmp[1]];
    }

    ClearList(attribute5);
}
