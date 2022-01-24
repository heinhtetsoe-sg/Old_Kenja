function btn_submit(cmd){

    selectLeft = document.forms[0].selectLeft;
    selectLeft.value = "";
    selectLeftText = document.forms[0].selectLeftText;
    selectLeftText.value = "";

    for (var i = 0; i < document.forms[0].RIGHT_PART.length; i++) {
        document.forms[0].RIGHT_PART.options[i].selected = 0;
    }

    sep = "";
    for (var i = 0; i < document.forms[0].LEFT_PART.length; i++) {
        document.forms[0].LEFT_PART.options[i].selected = 1;
        selectLeft.value = selectLeft.value + sep + document.forms[0].LEFT_PART.options[i].value;
        selectLeftText.value = selectLeftText.value + sep + document.forms[0].LEFT_PART.options[i].text;
        sep = ",";
    }

    if (cmd == "add" || cmd == "update" || cmd == "delete") {
        if (document.forms[0].COLLEGE_GRP_CD.value == ""){
            alert('{rval MSG301}' + '(グループコード)');
            return true;
        }
        if (document.forms[0].COLLEGE_GRP_NAME.value == ""){
            alert('{rval MSG301}' + '(グループ名)');
            return true;
        }
    }
    if (cmd == "add" || cmd == "update") {
        if (document.forms[0].LEFT_PART.length == 0){
            alert('対象学校を1件以上指定してください。');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function move1(side)
{   
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
        attribute1 = document.forms[0].RIGHT_PART;
        attribute2 = document.forms[0].LEFT_PART;
    } else {
        attribute1 = document.forms[0].LEFT_PART;
        attribute2 = document.forms[0].RIGHT_PART;  
    }

    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].value)+","+y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            tempaa[y] = String(attribute1.options[i].value)+","+y;
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

function moves(sides)
{   
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;

    if (sides == "left") {
        attribute5 = document.forms[0].RIGHT_PART;
        attribute6 = document.forms[0].LEFT_PART;
    } else {
        attribute5 = document.forms[0].LEFT_PART;
        attribute6 = document.forms[0].RIGHT_PART;  
    }

    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = String(attribute6.options[i].value)+","+z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = String(attribute5.options[i].value)+","+z;
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

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}

