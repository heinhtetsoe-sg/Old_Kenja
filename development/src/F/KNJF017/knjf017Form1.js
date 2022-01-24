function btn_submit(cmd) {
    if (cmd == 'changeGrade') {
        document.forms[0].selectdata.value = "";
    } else {
        //リストtoリスト
        selectdata = document.forms[0].selectdata;
        selectdata.value = "";
        sep = "";

        for (var i = 0; i < document.forms[0].LEFT_SELECT.length; i++) {
            var val = document.forms[0].LEFT_SELECT.options[i].value.split('-');
            selectdata.value = selectdata.value + sep + val[1];
            sep = ",";
        }
    }

    //データ指定チェック
    if (cmd == 'update') {
        var flg = false;
        var check_cnt = document.forms[0].CHECK_CNT.value;
        for (var i=0; i < document.forms[0].elements.length; i++) {
            for (var j=0; j <= check_cnt; j++) {
                if (document.forms[0].elements[i].name == 'RCHECK'+j) {
                    if (document.forms[0].elements[i].checked == true) {
                        flg = true;
                    }
                }
            }
        }

        //選択チェックボックス
        if (!flg) {
            alert('{rval MSG304}');
            return false;
        }

        //対象者
        if (document.forms[0].selectdata.value == "") {
            alert('{rval MSG304}');
            return false;
        }
    }

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //サブミット中、更新ボタン使用不可
//    document.forms[0].btn_update.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//全チェック操作
function check_all(obj) {
    var check_cnt = document.forms[0].CHECK_CNT.value;
    for (var i=0; i < document.forms[0].elements.length; i++) {
        for (var j=0; j <= check_cnt; j++) {
            if (document.forms[0].elements[i].name == 'RCHECK'+j) {
                document.forms[0].elements[i].checked = obj.checked;
            }
        }
    }
}

//視力(文字)
function Mark_Check(obj) {
    var mark = obj.value;
    var printKenkouSindanIppan = document.forms[0].printKenkouSindanIppan.value;
    var msg = "A～Dを入力して下さい。";
    if (printKenkouSindanIppan == '2') {
        msg = "A～D,未を入力して下さい。";
    }
    switch(mark) {
        case "a":
        case "A":
        case "ａ":
        case "Ａ":
            obj.value = "A";
            break;
        case "b":
        case "B":
        case "ｂ":
        case "Ｂ":
            obj.value = "B";
            break;
        case "c":
        case "C":
        case "ｃ":
        case "Ｃ":
            obj.value = "C";
            break;
        case "d":
        case "D":
        case "ｄ":
        case "Ｄ":
            obj.value = "D";
            break;
        case "":
            obj.value = "";
            break;
        case "未":
            if (printKenkouSindanIppan != '2') {
                alert(msg);
                obj.value = "";
            }
            break;
        default:
            alert(msg);
            obj.value = "";
            break;
    }
}

//所見
function syokenNyuryoku(obj, target_name) {
    if (obj.value == '') {
        var select_no = 0;
    } else {
        var select_no = parseInt(obj.value);
    }

    target_obj = document.forms[0][target_name];

    if (select_no < 2) {
        if (target_obj.value) {
            alert("テキストデータは更新時に削除されます");
        }
        target_obj.disabled = true;
    } else {
        target_obj.disabled = false;
    }
}

/**************************/
/*  リストtoリストの移動  */
/**************************/
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].right_select_l;
    ClearList(attribute,attribute);
    attribute = document.forms[0].left_select_l;
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
        attribute1 = document.forms[0].RIGHT_SELECT;
        attribute2 = document.forms[0].LEFT_SELECT;
    } else {
        attribute1 = document.forms[0].LEFT_SELECT;
        attribute2 = document.forms[0].RIGHT_SELECT;
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y]  = attribute2.options[i].value;
        tempa[y]  = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y=current1++
            temp1[y]  = attribute1.options[i].value;
            tempa[y]  = attribute1.options[i].text;
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
        attribute5 = document.forms[0].RIGHT_SELECT;
        attribute6 = document.forms[0].LEFT_SELECT;
    } else {
        attribute5 = document.forms[0].LEFT_SELECT;
        attribute6 = document.forms[0].RIGHT_SELECT;
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
