function btn_submit(cmd, columnname, patternDataDiv, itemname) {
    var link;
    if (cmd == 'teikei') {
        chr = document.forms[0].CHAIRCD.value;
        if (document.forms[0].KNJD133C_semesCombo) {
            sendSemester = document.forms[0].SEMESTER.value;
        } else {
            sendSemester = "";
        }

        link = 'knjd133cindex.php?cmd='+cmd+'&REPLACE_FLG=1&CHR='+chr+'&SEMESTER='+sendSemester+'&COLUMNNAME='+columnname+"&PATTERN_DATA_DIV="+patternDataDiv+"&ITEMNAME="+itemname;
        loadwindow(link, event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 450);
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_all(obj) {
    var i;
    for (i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/^RCHECK.*/)) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

//更新
function doSubmit() {
    var i;
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";

    if (document.forms[0].category_selected.length == 0 && document.forms[0].category_name.length == 0) {
        alert('{rval MSG916}');
        return false;
    }
    for (i = 0; i < document.forms[0].category_selected.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value.substring(9,17);
        sep = ",";
    }

    //更新中の画面ロック
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        updateFrameLock();
    }

    document.forms[0].cmd.value = 'replace_update';
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function move1(side) {
    var temp1 = [];
    var temp2 = [];
    var tempa = [];
    var tempb = [];
    var tempaa = [];
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;
    var i;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].category_name;
        attribute2 = document.forms[0].category_selected;
    } else {
        attribute1 = document.forms[0].category_selected;
        attribute2 = document.forms[0].category_name;
    }


    //fill an array with old values
    for (i = 0; i < attribute2.length; i++) {
        y=current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value+","+y;
        } else {
            y=current2++;
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    //generating new options
    for (i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1,attribute1);
    if (temp2.length > 0) {
        for (i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }

}

function moves(sides) {
    var temp5 = [];
    var tempc = [];
    var tempaa = [];
    var current5 = 0;
    var z=0;
    var i;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].category_name;
        attribute6 = document.forms[0].category_selected;
    } else {
        attribute5 = document.forms[0].category_selected;
        attribute6 = document.forms[0].category_name;
    }

    //fill an array with old values
    for (i = 0; i < attribute6.length; i++) {
        z=current5++;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z;
    }

    //assign new values to arrays
    for (i = 0; i < attribute5.length; i++) {
        z=current5++;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value+","+z;
    }

    tempaa.sort();

    //generating new options
    for (i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5,attribute5);
}
