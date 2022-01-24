function btn_submit(cmd) {
    //右のリストtoリスト
    selectdata = document.forms[0].selectdata_r;
    selectdata.value = "";
    sep = "";

    for (var i = 0; i < document.forms[0].left_select_r.length; i++) {
        selectdata.value = selectdata.value + sep + document.forms[0].left_select_r.options[i].value;
        sep = ",";
    }

    //左のリストtoリスト
    selectdata = document.forms[0].selectdata_l;
    selectdata.value = "";
    sep = "";

    for (var i = 0; i < document.forms[0].left_select_l.length; i++) {
        if (document.forms[0].TAISYOSENTAKU2.checked == true) {
            selectdata.value = selectdata.value + sep + document.forms[0].left_select_l.options[i].value.substring(9);
        } else {
            selectdata.value = selectdata.value + sep + document.forms[0].left_select_l.options[i].value;
        }
            sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit() {
    //右のリストtoリスト
    selectdata = document.forms[0].selectdata_r;
    selectdata.value = "";
    sep = "";
    if (document.forms[0].left_select_r.length==0) {
        alert('{rval MSG304}'+'（書出し項目）');
        return true;
    }
    for (var i = 0; i < document.forms[0].left_select_r.length; i++) {
        selectdata.value = selectdata.value + sep + document.forms[0].left_select_r.options[i].value;
        sep = ",";
    }

    //左のリストtoリスト
    selectdata = document.forms[0].selectdata_l;
    selectdata.value = "";
    sep = "";

    if (document.forms[0].left_select_l.length==0) {
        alert('{rval MSG304}'+'（出力対象者）');
        return true;
    }

    for (var i = 0; i < document.forms[0].left_select_l.length; i++) {
        if (document.forms[0].TAISYOSENTAKU2.checked == true) {
            selectdata.value = selectdata.value + sep + document.forms[0].left_select_l.options[i].value.substring(9);
        } else {
            selectdata.value = selectdata.value + sep + document.forms[0].left_select_l.options[i].value;
        }
        sep = ",";
    }

    if (document.forms[0].TAISYOSENTAKU2.checked == true) {
        document.forms[0].SCHREGNO.value = selectdata.value;
    } else {
        document.forms[0].GRADE.value = selectdata.value;
    }
    document.forms[0].cmd.value = 'csv';
    document.forms[0].submit();
    return false;
}

function temp_clear() {
    ClearList(document.forms[0].left_select,document.forms[0].left_select);
    ClearList(document.forms[0].right_select,document.forms[0].right_select);
}

/****************************************************************************/
/****************************************************************************/
/****************************** リストtoリスト ******************************/
/****************************************************************************/
/****************************************************************************/
function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName)
{
    attribute = document.forms[0].right_select_l;
    ClearList(attribute,attribute);
    attribute = document.forms[0].left_select_l;
    ClearList(attribute,attribute);
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

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].right_select_l;
        attribute2 = document.forms[0].left_select_l;
    } else {
        attribute1 = document.forms[0].left_select_l;
        attribute2 = document.forms[0].right_select_l;
    }


    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].value).substring(0,8)+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected )
        {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = String(attribute1.options[i].value).substring(0,8)+","+y;
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
function moves(sides)
{
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].right_select_l;
        attribute6 = document.forms[0].left_select_l;
    } else {
        attribute5 = document.forms[0].left_select_l;
        attribute6 = document.forms[0].right_select_l;
    }


    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = String(attribute6.options[i].value).substring(0,8)+","+z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = String(attribute5.options[i].value).substring(0,8)+","+z;
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
