function btn_submit(cmd) {
    if (cmd == 'update') {
        //左のリスト保持
        attribute3 = document.forms[0].selectdataL;
        attribute3.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
            sep = ",";
        }

        //右のリスト保持
        attribute4 = document.forms[0].selectdataR;
        attribute4.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
            attribute4.value = attribute4.value + sep + document.forms[0].CATEGORY_NAME.options[i].value;
            sep = ",";
        }
    }

    //取消確認
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//日付変更
function tmp_list(cmd, submit) {
    document.forms[0].cmd.value = cmd;
    if (submit == 'on') {
        document.forms[0].submit();
        return false;
    }
}

//リスト内の背景色変更
function change_bgcolor(color_no, color) {
    var cleft = new Array();
    var cright = new Array();

    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        cleft = document.forms[0].CATEGORY_SELECTED.options[i].value.split('-');
        if (cleft[color_no] == 1) {
            document.forms[0].CATEGORY_SELECTED.options[i].style.backgroundColor = color;
        }
    }
    for (var i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
        cright = document.forms[0].CATEGORY_NAME.options[i].value.split('-');
        if (cright[color_no] == 1) {
            document.forms[0].CATEGORY_NAME.options[i].style.backgroundColor = color;
        }
    }
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

function move1(side, color) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;

    var chg_val = new String();
    var chg_txt = new Array();
    var chg_flg = new String();

    //預かり費用
    for (var i = 0; i < document.forms[0].FARE_CD.length; i++) {
        if (document.forms[0].FARE_CD[i].checked) {
            chg_val = document.forms[0].FARE_CD[i].value;
        }
    }
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/^FARE_SHOW/)) {
            var chg = document.forms[0].elements[i].name.split('-');
            chg_txt[chg[1]] = document.forms[0].elements[i].value;
        }
    }

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
        var attribute1_cnt = document.getElementById('CATEGORY_NAME');
        var attribute2_cnt = document.getElementById('CATEGORY_SELECTED');
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;
        var attribute1_cnt = document.getElementById('CATEGORY_SELECTED');
        var attribute2_cnt = document.getElementById('CATEGORY_NAME');
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        val = attribute2.options[i].value.split('-');
        tempaa[y] = String(val[1]+"-"+val[0])+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++

            if (side == "left") {
                val = attribute1.options[i].value.split('-');
                txt = attribute1.options[i].text.split(':');

                //預かり費用セット
                temp1[y] = val[0]+"-"+val[1]+"-"+chg_val+"-1-"+val[4];
                tempa[y] = txt[0]+": ("+chg_txt[chg_val]+")"+txt[1];
                tempaa[y] = String(val[1]+"-"+val[0])+","+y;
            } else {
                val = attribute1.options[i].value.split('-');
                txt = attribute1.options[i].text.split(':');
                txt2 = txt[1].split(')');

                //背景色変更フラグ
                if (val[4] == "R") {
                    chg_flg = 0;
                } else {
                    chg_flg = 1;
                }

                //預かり費用カット
                temp1[y] = val[0]+"-"+val[1]+"--"+chg_flg+'-'+val[4];
                tempa[y] = txt[0]+": "+txt2[1];
                tempaa[y] = String(val[1]+"-"+val[0])+","+y;
            }
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    //人数表示セット
    attribute1_cnt.innerHTML = temp2.length;
    attribute2_cnt.innerHTML = temp1.length;

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];

        //背景色セット
        var list_value = temp1[tmp[1]].split('-');
        if (list_value[3] == 1) {
            attribute2.options[i].style.backgroundColor = color;
        }
    }

    //generating new options
    ClearList(attribute1,attribute1);
    if (temp2.length > 0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];

            //背景色セット
            var list_value = temp2[i].split('-');
            if (list_value[3] == 1) {
                attribute1.options[i].style.backgroundColor = color;
            }
        }
    }
}

function moves(sides, color) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;

    var chg_val = new String();
    var chg_txt = new Array();
    var chg_flg = new String();

    //預かり費用
    for (var i = 0; i < document.forms[0].FARE_CD.length; i++) {
        if (document.forms[0].FARE_CD[i].checked) {
            chg_val = document.forms[0].FARE_CD[i].value;
        }
    }
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/^FARE_SHOW/)) {
            var chg = document.forms[0].elements[i].name.split('-');
            chg_txt[chg[1]] = document.forms[0].elements[i].value;
        }
    }

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
        var attribute5_cnt = document.getElementById('CATEGORY_NAME');
        var attribute6_cnt = document.getElementById('CATEGORY_SELECTED');
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;
        var attribute5_cnt = document.getElementById('CATEGORY_SELECTED');
        var attribute6_cnt = document.getElementById('CATEGORY_NAME');
    }

    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        val = attribute6.options[i].value.split('-');
        tempaa[z] = String(val[1]+"-"+val[0])+","+z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z=current5++

        if (sides == "left") {
            val = attribute5.options[i].value.split('-');
            txt = attribute5.options[i].text.split(':');

            //預かり費用セット
            temp5[z] = val[0]+"-"+val[1]+"-"+chg_val+"-1-"+val[4];
            tempc[z] = txt[0]+": ("+chg_txt[chg_val]+")"+txt[1];
            tempaa[z] = String(val[1]+"-"+val[0])+","+z;
        } else {
            val = attribute5.options[i].value.split('-');
            txt = attribute5.options[i].text.split(':');
            txt2 = txt[1].split(')');

            //背景色変更フラグ
            if (val[4] == "R") {
                chg_flg = 0;
            } else {
                chg_flg = 1;
            }

            //預かり費用カット
            temp5[z] = val[0]+"-"+val[1]+"--"+chg_flg+'-'+val[4];
            tempc[z] = txt[0]+": "+txt2[1];
            tempaa[z] = String(val[1]+"-"+val[0])+","+z;
        }
    }

    //人数表示セット
    attribute5_cnt.innerHTML = 0;
    attribute6_cnt.innerHTML = temp5.length;

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];

        //背景色セット
        var list_value = temp5[tmp[1]].split('-');
        if (list_value[3] == 1) {
            attribute6.options[i].style.backgroundColor = color;
        }
    }

    //generating new options
    ClearList(attribute5,attribute5);
}
