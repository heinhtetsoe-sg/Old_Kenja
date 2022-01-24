function btn_submit(cmd) {
    //チェック
    rightList = document.forms[0].CATEGORY_NAME;
    leftList  = document.forms[0].CATEGORY_SELECTED;

    //対象者一覧
    attribute = document.forms[0].selectdata;
    attribute.value = "";
    sep = "";
    if (leftList.length > 0){
        for (var i = 0; i < leftList.length; i++) {
            attribute.value = attribute.value + sep + leftList.options[i].value;
            sep = ",";
        }
    }

    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }
    if (cmd == 'update' || cmd == 'delete') {
        if (document.forms[0].QUALIFIED_CD.value == '') {
            alert('{rval MSG301}');
            return false;
        }
    }
    if (cmd == 'update') {
        if (document.forms[0].LIMIT_MONTH.value == '' || document.forms[0].SETUP_CNT.value == '') {
            alert('{rval MSG301}');
            return false;
        }
        if (document.forms[0].CATEGORY_SELECTED.length == 0) {
            alert('{rval MSG916}');
            return;
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

//グループコード入力チェック
function checkGroupcd(obj) {
    //数字チェック
    if (isNaN(obj.value)) {
        alert('{rval MSG907}');
        obj.value = obj.defaultValue;
        return;
    }

    var groupcd = parseInt(obj.value);
    if (groupcd >= 600) {
        alert('{rval MSG914}'+'（600未満）\n追加はできません。');
        obj.value = obj.defaultValue;
    }
    if (obj.value >= 600) {
        flg = true;
    } else {
        flg = false;
    }

    document.forms[0].GRADE.disabled = flg;
    document.forms[0].COURSEMAJOR.disabled = flg;
    document.forms[0].COURSECODE.disabled = flg;

    document.forms[0].CLASSCD.disabled = flg;
    document.forms[0].CATEGORY_NAME.disabled = flg;
    document.forms[0].CATEGORY_SELECTED.disabled = flg;

    document.forms[0].btn_lefts.disabled = flg;
    document.forms[0].btn_left1.disabled = flg;
    document.forms[0].btn_right1.disabled = flg;
    document.forms[0].btn_rights.disabled = flg;

    document.forms[0].btn_add.disabled = flg;

    return;
}

/**************************************************************************************************************/
/******************************************* 以下リストtoリスト関係 *******************************************/
/**************************************************************************************************************/
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

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;
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
        if ( attribute1.options[i].selected )
        {
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

function moves(sides) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;
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

    tempaa.sort();

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
