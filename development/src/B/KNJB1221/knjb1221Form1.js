function btn_submit(cmd) {

    if (cmd == "update") {
        if (document.forms[0].CHAIR_SELECTED.length == 0) {
            alert('{rval MSG304}\n(履修講座一覧)');
            return false;
        }

        if (document.forms[0].STD_SELECTED.length == 0) {
            alert('{rval MSG304}\n(更新対象一覧)');
            return false;
        }

        var sdate = document.forms[0].sdate.value.split('-').join('/');
        var edate = document.forms[0].edate.value.split('-').join('/');
        if(sdate > document.forms[0].DATE.value || edate < document.forms[0].DATE.value){
            alert('開始日は' + sdate + '～' + edate + 'の範囲で指定してください。');
            return false;
        }
    }

    var attribute1 = document.forms[0].selectdataStd;
    var attribute2 = document.forms[0].selectdataStdText;
    if (cmd == "changeStudent" || cmd == "update") {
        var sep = "";
        for (var i = 0; i < document.forms[0].STD_SELECTED.length; i++) {
            attribute1.value = attribute1.value + sep + document.forms[0].STD_SELECTED.options[i].value;
            attribute2.value = attribute2.value + sep + document.forms[0].STD_SELECTED.options[i].label;
            sep = "|";
        }
    }

    var attribute3 = document.forms[0].selectdataChair;
    var attribute4 = document.forms[0].selectdataChairText;
    if (cmd == "changeTrgtHr" || cmd == "update") {
        var sep = "";
        for (var i = 0; i < document.forms[0].CHAIR_SELECTED.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].CHAIR_SELECTED.options[i].value;
            attribute4.value = attribute4.value + sep + document.forms[0].CHAIR_SELECTED.options[i].label;
            sep = "|";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//開始日変更
function tmp_list(cmd, submit) {
    var attribute1 = document.forms[0].selectdataStd;
    var attribute2 = document.forms[0].selectdataStdText;
    var attribute3 = document.forms[0].selectdataChair;
    var attribute4 = document.forms[0].selectdataChairText;

    if (cmd == "changeDate") {
        var sep = "";
        for (var i = 0; i < document.forms[0].CHAIR_SELECTED.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].CHAIR_SELECTED.options[i].value;
            attribute4.value = attribute4.value + sep + document.forms[0].CHAIR_SELECTED.options[i].label;
            sep = "|";
        }
    } else {
        sep = "";
        for (var i = 0; i < document.forms[0].STD_SELECTED.length; i++) {
            attribute1.value = attribute1.value + sep + document.forms[0].STD_SELECTED.options[i].value;
            attribute2.value = attribute2.value + sep + document.forms[0].STD_SELECTED.options[i].text;
            sep = "|";
        }
    }

    document.forms[0].cmd.value = cmd;
    if (submit == 'on') {
        document.forms[0].submit();
        return false;
    }
}

function move1(side, categoryName, categorySelected) {
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
        attribute1 = document.forms[0][categoryName];
        attribute2 = document.forms[0][categorySelected];
    } else {
        attribute1 = document.forms[0][categorySelected];
        attribute2 = document.forms[0][categoryName];
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

function moves(sides, categoryName, categorySelected) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;

    if (sides == "left") {
        attribute5 = document.forms[0][categoryName];
        attribute6 = document.forms[0][categorySelected];
    } else {
        attribute5 = document.forms[0][categorySelected];
        attribute6 = document.forms[0][categoryName];
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
