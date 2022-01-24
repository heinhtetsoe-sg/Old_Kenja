function btn_submit(cmd) {
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //更新
    if (cmd == 'add' || cmd == 'update') {
        if (document.forms[0].TESTDIV.value == '') {
            alert('{rval MSG304}\n( 入試種別 )');
            return;
        }
        if (document.forms[0].EXAM_TYPE.value == '') {
            alert('{rval MSG304}\n( 入試方式 )');
            return;
        }
        if (document.forms[0].TEST_DATE.value == '') {
            alert('{rval MSG304}\n( 試験日 )');
            return;
        }
        if (document.forms[0].DISTINCT_ID.value == '') {
            alert('{rval MSG304}\n( 入試判別ID )');
            return;
        }
        if (document.forms[0].DISTINCT_NAME.value == '') {
            alert('{rval MSG304}\n( 入試判別名称 )');
            return;
        }

        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        if (document.forms[0].CATEGORY_SELECTED.length == 0) {
            alert('{rval MSG916}');
            return false;
        }
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
            sep = ",";
        }
        rightData = document.forms[0].selectRightdata;
        rightData.value = "";
        sep = "";
        if (document.forms[0].CATEGORY_NAME.length != 0) {
            for (var i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
                rightData.value = rightData.value + sep + document.forms[0].CATEGORY_NAME.options[i].value;
                sep = ",";
            }
        }
        document.getElementById('marq_msg').style.color = '#FF0000';
    }

    //読込中は、追加・更新・削除ボタンをグレーアウト
    document.forms[0].btn_add.disabled      = true;
    document.forms[0].btn_update.disabled   = true;
    document.forms[0].btn_del.disabled      = true;
//    document.forms[0].btn_reset.disabled    = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
/***************************************************************************/
/**************************** List to List 関係 ****************************/
/***************************************************************************/
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
        tempaa[y] = String(attribute2.options[i].text).substring(0,12)+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected )
        {  
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            tempaa[y] = String(attribute1.options[i].text).substring(0,12)+","+y;
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
        tempaa[z] = String(attribute6.options[i].text).substring(0,12)+","+z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = String(attribute5.options[i].text).substring(0,12)+","+z;
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
