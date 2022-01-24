function btn_submit(cmd) {

    if (cmd == 'add' || cmd == 'update' || cmd == 'delete') {
        //必須入力チェック
        if (!document.forms[0].CLUBCD.value) {
            alert('{rval MSG916}\n　　( 部クラブ )');
            return;
        }
        if (!document.forms[0].DETAIL_DATE.value) {
            alert('{rval MSG916}\n　　　( 日付 )');
            return;
        }
        if (document.forms[0].DIV[0].checked == true) {
            if (!document.forms[0].SCHREGNO.value) {
                alert('{rval MSG916}\n　　　( 生徒 )');
                return;
            }
        }

        //日付範囲チェック
        sdate = document.forms[0].YEAR.value + '/04/01';
        edate = (Number(document.forms[0].YEAR.value)+1) + '/03/31';
        if (document.forms[0].DETAIL_DATE.value < sdate || document.forms[0].DETAIL_DATE.value > edate) {
            alert('{rval MSG916}\n(日付： '+sdate+' ～ '+edate+' )');
            return;
        }
    }

    //削除
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }

    //取消
    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')) {
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }

    //団体
    if (document.forms[0].DIV[1].checked == true) {
        if (cmd != 'edit3') {
            document.forms[0].selectdata.value = "";
            sep = "";
            for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
                document.forms[0].selectdata.value = document.forms[0].selectdata.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
                sep = ",";
            }
        }

        if (cmd == 'add' || cmd == 'update') {
            if (document.forms[0].CATEGORY_SELECTED.length == 0 && document.forms[0].CATEGORY_NAME.length == 0) {
                alert('{rval MSG916}\n生徒が所属していません。');
                return false;
            }
            if (document.forms[0].CATEGORY_SELECTED.length == 0) {
                alert('{rval MSG916}\n対象生徒を選択してください。');
                return;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//大会名反映
function refl() {
    document.forms[0].MEET_NAME.value = document.forms[0].MEETLIST.value;
}

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
    var y = 0;
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
        y = current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value+","+y;
        } else {
            y = current2++
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
    if (temp2.length > 0) {
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
    var z = 0;

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
        z = current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z = current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value+","+z;
    }

    tempaa.sort();

    //generating new options
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
