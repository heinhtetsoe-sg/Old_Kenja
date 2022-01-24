function btn_submit(cmd) {

    //右のリストtoリスト
    selectdata = document.forms[0].selectdata_r;
    selectdata.value = "";
    sep = "";

    for (var i = 0; i < document.forms[0].left_select_r.length; i++) {
        var val = document.forms[0].left_select_r.options[i].value.split('-');
        selectdata.value = selectdata.value + sep + val[1];
        sep = ",";
    }

    //左のリストtoリスト
    selectdata = document.forms[0].selectdata_l;
    selectdata.value = "";
    sep = "";

    for (var i = 0; i < document.forms[0].left_select_l.length; i++) {
        selectdata.value = selectdata.value + sep + document.forms[0].left_select_l.options[i].value.substring(9);
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit() {
    if (document.forms[0].MONTH.value == "") {
        alert('{rval MSG304}'+'（対象月）');
        return true;
    }

    //右のリストtoリスト
    selectdata = document.forms[0].selectdata_r;
    selectdata.value = "";
    sep = "";
    if (document.forms[0].left_select_r.length == 0) {
        alert('{rval MSG304}'+'（書出し項目）');
        return true;
    }
    if (document.forms[0].left_select_r.length > 20) {
        alert('{rval MSG915}'+'\n20項目までです。');
        return true;
    }

    for (var i = 0; i < document.forms[0].left_select_r.length; i++) {
        var val = document.forms[0].left_select_r.options[i].value.split('-');
        selectdata.value = selectdata.value + sep + val[1];
        sep = ",";
    }

    //左のリストtoリスト
    selectdata = document.forms[0].selectdata_l;
    selectdata.value = "";
    sep = "";

    if (document.forms[0].left_select_l.length == 0) {
        alert('{rval MSG304}'+'（出力対象者）');
        return true;
    }

    for (var i = 0; i < document.forms[0].left_select_l.length; i++) {
        selectdata.value = selectdata.value + sep + document.forms[0].left_select_l.options[i].value.substring(9);
        sep = ",";
    }

    document.forms[0].SCHREGNO.value = selectdata.value;
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
function move1(side, div)
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
        attribute1 = document.forms[0]['right_select_'+div];
        attribute2 = document.forms[0]['left_select_'+div];
    } else {
        attribute1 = document.forms[0]['left_select_'+div];
        attribute2 = document.forms[0]['right_select_'+div];
    }


    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        if (div == 'l') {
            tempaa[y] = String(attribute2.options[i].value).substring(0,8)+","+y;
        } else {
            tempaa[y] = attribute2.options[i].value+","+y;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected )
        {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            if (div == 'l') {
                tempaa[y] = String(attribute1.options[i].value).substring(0,8)+","+y;
            } else {
                tempaa[y] = attribute1.options[i].value+","+y;
            }
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    if (!(side == "left" && div == "r")) {
        tempaa.sort();
    }

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
function moves(sides, div)
{
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0]['right_select_'+div];
        attribute6 = document.forms[0]['left_select_'+div];
    } else {
        attribute5 = document.forms[0]['left_select_'+div];
        attribute6 = document.forms[0]['right_select_'+div];
    }


    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        if (div == 'l') {
            tempaa[z] = String(attribute6.options[i].value).substring(0,8)+","+z;
        } else {
            tempaa[z] = attribute6.options[i].value+","+z;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        if (div == 'l') {
            tempaa[z] = String(attribute5.options[i].value).substring(0,8)+","+z;
        } else {
            tempaa[z] = attribute5.options[i].value+","+z;
        }
    }

    if (!(sides == "left" && div == "r")) {
        tempaa.sort();
    }

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

//画面の切替
function Page_jumper(link, schoolKind) {

    selectdata = document.forms[0].selectdata_l;
    selectdata.value = "";
    sep = "";

    for (var i = 0; i < document.forms[0].left_select_l.length; i++) {
        selectdata.value = selectdata.value + sep + document.forms[0].left_select_l.options[i].value.substring(9);
        sep = ",";
    }

    cd = document.forms[0].SUBSYSTEM.value;
    ubar = (cd == "") ? "" : "_";
    g = document.forms[0].GRADE.value;
    ghr = document.forms[0].GRADE_HR_CLASS.value;
    if (document.forms[0].OUTPUT[0].checked) {
        output  = document.forms[0].OUTPUT[0].value;
    } else if (document.forms[0].OUTPUT[1].checked) {
        output  = document.forms[0].OUTPUT[1].value;
    } else {
        output  = document.forms[0].OUTPUT[2].value;
    }

    link = link + "/I/KNJI100C" + ubar + cd + "/knji100c" + ubar + cd + "index.php?SUBSYSTEM=" + cd;
    link = link + "&selectdata_l=" + selectdata.value + "&GRADE=" + g + "&GRADE_HR_CLASS=" + ghr + "&OUTPUT=" + output;
    link = link + "&SEND_selectSchoolKind=" + schoolKind;

    parent.location.href=link;
}
