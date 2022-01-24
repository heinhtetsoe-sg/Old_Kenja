function btn_submit(cmd) {
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
            return false;
    }

    if (cmd == 'list') {
        window.open('knjz233index.php?cmd=sel&init=1','right_frame');
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function doSubmit() {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].classyear.length == 0 && document.forms[0].classmaster.length == 0) {
        alert('{rval MSG916}');
        return false;
    }
    for (var i = 0; i < document.forms[0].classyear.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].classyear.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = 'check';
    document.forms[0].submit();
    return false;
}

function Show_Confirm()
{
    if (!confirm("成績処理がされてます。変更しますか？\n変更する場合は成績処理を再度実行してください。")) {
        document.forms[0].record_dat_flg.value = '0';
        document.forms[0].cmd.value = 'sel';
        document.forms[0].submit();
        return false;
    } else {
        document.forms[0].record_dat_flg.value = '1';
        document.forms[0].cmd.value = 'update';
        document.forms[0].submit();
        return false;
    }
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}

function move(side)
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
        attribute1 = document.forms[0].classmaster;
        attribute2 = document.forms[0].classyear;
    } else {
        attribute1 = document.forms[0].classyear;
        attribute2 = document.forms[0].classmaster;  
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

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].classyear.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].classyear.options[i].value;
        sep = ",";
    }
}

function OnAuthError()
{
    alert('{rval MZ0026}');
    closeWin();
}

function moves(side)
{
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    if (side == "sel_add_all") {
        attribute5 = document.forms[0].classmaster;
        attribute6 = document.forms[0].classyear;
    } else {
        attribute5 = document.forms[0].classyear;
        attribute6 = document.forms[0].classmaster;  
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
