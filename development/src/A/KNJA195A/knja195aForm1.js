function btn_submit(cmd) {

    attribute3 = document.forms[0].selectleft;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
        attribute3.value = attribute3.value + sep + document.forms[0].CLASS_SELECTED.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

    //出力対象チェック
    if (document.forms[0].CLASS_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return;
    }

    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
    
function AllClearList(OptionList, TitleName) 
{
    attribute = document.forms[0].CLASS_NAME;
    ClearList(attribute,attribute);
    attribute = document.forms[0].CLASS_SELECTED;
    ClearList(attribute,attribute);
}

function move1(side,chdt)
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
        if (chdt == 2) {
            tempaa[y] = String(attribute2.options[i].value).substr(String(attribute2.options[i].value).indexOf("-"))+","+y; 
        } else {
            tempaa[y] = String(attribute2.options[i].value)+","+y;
        }
    }

    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            if (chdt == 2){
                tempaa[y] = String(attribute1.options[i].value).substr(String(attribute1.options[i].value).indexOf("-"))+","+y; 
            }else {
                tempaa[y] = String(attribute1.options[i].value)+","+y;
            }
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

function moves(sides,chdt)
{   
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
        if (chdt == 2) {
            tempaa[z] = String(attribute6.options[i].value).substr(String(attribute6.options[i].value).indexOf("-"))+","+z; 
        } else {
            tempaa[z] = String(attribute6.options[i].value)+","+z;
        }
    }

    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        if (chdt == 2) {
            tempaa[z] = String(attribute5.options[i].value).substr(String(attribute5.options[i].value).indexOf("-"))+","+z; 
        } else {
            tempaa[z] = String(attribute5.options[i].value)+","+z;
        }
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

function DataUse(obj1)
{
    if ((document.forms[0].CHECK3.checked == true) || (document.forms[0].CHECK4.checked == true)) {
        flag1 = false;
    } else {
        flag1 = true;
    }
    document.forms[0].DATE.disabled = flag1;
    document.forms[0].btn_calen.disabled = flag1;
}


function SelectUse(obj2)
{
    if (document.forms[0].CHECK9.checked == true) {
        document.forms[0].SELECT1.disabled = false;
        document.forms[0].SELECT2.disabled = false;
    } else {
        document.forms[0].SELECT1.disabled = true;
        document.forms[0].SELECT2.disabled = true;
    }
}


function OptionUse(obj3)
{
    if (document.forms[0].CHECK6.checked == true) {
        document.forms[0].OUTPUT[0].disabled = false;
        document.forms[0].OUTPUT[1].disabled = false;
    } else {
        document.forms[0].OUTPUT[0].disabled = true;
        document.forms[0].OUTPUT[1].disabled = true;
    }
}

function OptionUse2(obj4)
{
    if (document.forms[0].CHECK1.checked == true) {
        document.forms[0].OUTPUTA[0].disabled = false;
        document.forms[0].OUTPUTA[1].disabled = false;
        document.forms[0].PRINT_STAMP.disabled = false;
        document.forms[0].useForm9_PJ_Ippan.disabled = false;
    } else {
        document.forms[0].OUTPUTA[0].disabled = true;
        document.forms[0].OUTPUTA[1].disabled = true;
        document.forms[0].PRINT_STAMP.disabled = true;
        document.forms[0].useForm9_PJ_Ippan.disabled = true;
    }
}


function OptionUse3(obj5)
{
    if (document.forms[0].CHECK2.checked == true) {
        document.forms[0].OUTPUTB[0].disabled = false;
        document.forms[0].OUTPUTB[1].disabled = false;
        document.forms[0].useForm9_PJ_Ha.disabled = false;
    } else {
        document.forms[0].OUTPUTB[0].disabled = true;
        document.forms[0].OUTPUTB[1].disabled = true;
        document.forms[0].useForm9_PJ_Ha.disabled = true;
    }
}

function dis_date(flag)
{
    document.forms[0].DATE.disabled = flag;
    document.forms[0].btn_calen.disabled = flag;
}

function dis_date5()
{
    var flag = !document.forms[0].CHECK5.checked;
    document.forms[0].DATE5.disabled = flag;
    document.forms[0].btn_calen.disabled = flag;
}
