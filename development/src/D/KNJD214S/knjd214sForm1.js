function btn_submit(cmd) {
    if (cmd == 'del_rireki') {
        if (document.all("DEL_CHECK[]") == null) {
            return false;
        }
        //選択チェック
        for (var i=0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name == "DEL_CHECK[]" && document.forms[0].elements[i].checked) {
                break;
            }
        }
        if (i == document.forms[0].elements.length) {
            alert('「削除チェックボックス」を選択してください。');
            return true;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function disCmb() {
    var useProvFlg = document.forms[0].useProvFlg.value;
    if (document.forms[0].SHORI[0].checked) {
        document.forms[0].TESTKINDCD.disabled = true;
        if (useProvFlg == '1') {
            document.forms[0].KARI_DIV[0].disabled = true;
            document.forms[0].KARI_DIV[1].disabled = true;
        }
    } else {
        document.forms[0].TESTKINDCD.disabled = false;
        if (useProvFlg == '1') {
            document.forms[0].KARI_DIV[0].disabled = false;
            document.forms[0].KARI_DIV[1].disabled = false;
        }
    }
}
function doSubmit() {
    //チェック
    leftList  = document.forms[0].CLASS_SELECTED;
    if (leftList.length == 0) {
        alert('クラスを選択して下さい。');
        return false;
    }
    //対象者一覧
    attribute4 = document.forms[0].selectdata;
    attribute4.value = "";
    sep = "";
    for (var i = 0; i < leftList.length; i++)
    {
        attribute4.value = attribute4.value + sep + leftList.options[i].value;
        sep = ",";
    }
    if (document.forms[0].SHORI[0].checked) {
        document.forms[0].cmd.value = 'clear';
    } else {
        document.forms[0].cmd.value = 'copy';
    }
    document.forms[0].submit();
    return false;
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
    var y=0;
    var attribute;
    
    if (side == "left") {
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;  
    }

    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
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

    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

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
    
    if (sides == "left") {
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute5 = document.forms[0].CLASS_SELECTED;
        attribute6 = document.forms[0].CLASS_NAME;  
    }

    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z;
    }

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

    ClearList(attribute5,attribute5);

}
