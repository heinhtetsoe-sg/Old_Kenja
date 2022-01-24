function btn_submit(cmd) {
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
            return false;
    }
    if (cmd == 'list') {
        window.open('knjh537aindex.php?cmd=sel','right_frame');
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit(cmd) {

    if (document.forms[0].PROFICIENCY_SUBCLASS_CD.value == ""){
        alert('{rval MSG304}' + '\n(実力科目)');
        return false;
    }
    if (document.forms[0].GRADE.value == ""){
        alert('{rval MSG304}' + '\n(学年)');
        return false;
    }
    if (document.forms[0].COURSE.value == "") {
        div = (document.forms[0].COURSE_DIV.value == "1") ? '課程学科コース' : 'コースグループ';
        alert('{rval MSG304}' + '\n(' + div + ')');
        return false;
    }

    attribute = document.forms[0].selectdata;
    attribute.value = "";
    sep = "";
    if (document.forms[0].LSUBCLASS.length == 0) {
        if (!confirm('{rval MSG103}'))
            return false;
    }
    for (var i = 0; i < document.forms[0].LSUBCLASS.length; i++) {
        attribute.value = attribute.value + sep + document.forms[0].LSUBCLASS.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function move(side, name) {
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
        attribute1 = document.forms[0].RSUBCLASS;
        attribute2 = document.forms[0].LSUBCLASS;
    } else {
        attribute1 = document.forms[0].LSUBCLASS;
        attribute2 = document.forms[0].RSUBCLASS;
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

function moves(side, name) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    if (side == "sel_add_all") {
        attribute5 = document.forms[0].RSUBCLASS;
        attribute6 = document.forms[0].LSUBCLASS;
    } else {
        attribute5 = document.forms[0].LSUBCLASS;
        attribute6 = document.forms[0].RSUBCLASS;
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

    //generating new options
    ClearList(attribute5,attribute5);
}
