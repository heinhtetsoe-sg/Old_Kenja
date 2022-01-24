function btn_submit(cmd) {
    //取消
    if (cmd == "clear" && !confirm('{rval MSG106}')) {
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

//更新
function doSubmit() {
    //データ有無チェック
    if (document.forms[0].LEFT_LIST.length == 0 && document.forms[0].RIGHT_LIST.length == 0) {
        alert('{rval MSG303}');
        return false;
    }

    //合格者を格納
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].LEFT_LIST.length > 0) {
        for (var i = 0; i < document.forms[0].LEFT_LIST.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].LEFT_LIST.options[i].value;
            sep = ",";
        }
    }

    //受検者を格納
    attribute4 = document.forms[0].selectdata2;
    attribute4.value = "";
    sep = "";
    if (document.forms[0].RIGHT_LIST.length > 0) {
        for (var i = 0; i < document.forms[0].RIGHT_LIST.length; i++) {
            attribute4.value = attribute4.value + sep + document.forms[0].RIGHT_LIST.options[i].value;
            sep = ",";
        }
    }

    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;
}


/*************************/
/**  以下 LIST to LIST  **/
/*************************/
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function move3(direction, left_name, right_name, flg) {
    move2(direction, left_name, right_name, flg);
    return;
}

function move2(side, left, right, sort) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var a = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute1;
    var attribute2;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all") {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    } else {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        a[temp1[y]] = tempa[y];
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (side == "right" || side == "left") {
            if ( attribute1.options[i].selected ) {
                y=current1++
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].text;
                a[temp1[y]] = tempa[y];
            } else {
                y=current2++
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        } else {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            a[temp1[y]] = tempa[y];
        }
    }
    if (sort) {
        //sort
        temp1 = temp1.sort();
        //generating new options
        for (var i = 0; i < temp1.length; i++) {
            tempa[i] = a[temp1[i]];
        }
    }

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text =  tempa[i];
    }

    //generating new options
    ClearList(attribute1);
    if (temp2.length>0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0][left].length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0][left].options[i].value;
        sep = ",";
    }
}
