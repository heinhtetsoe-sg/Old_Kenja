function btn_submit(cmd) {

    //ＣＳＶ出力
    if (cmd == "csv") {
        if (document.forms[0].COMMI_SELECTED.length == 0) {
            alert('{rval MSG916}');
            return false;
        }

        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].COMMI_NAME.length; i++) {
            document.forms[0].COMMI_NAME.options[i].selected = 0;
        }

        for (var i = 0; i < document.forms[0].COMMI_SELECTED.length; i++) {
            document.forms[0].COMMI_SELECTED.options[i].selected = 1;
            attribute3.value = attribute3.value + sep + document.forms[0].COMMI_SELECTED.options[i].value;
            sep = ",";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function newwin(SERVLET_URL){

    if (document.forms[0].COMMI_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return false;
    }
    if(document.forms[0].J004.value == "") {
        alert('対象学期を指定して下さい。');
        return false;
    }

    for (var i = 0; i < document.forms[0].COMMI_NAME.length; i++) {
        document.forms[0].COMMI_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].COMMI_SELECTED.length; i++) {
        document.forms[0].COMMI_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJJ";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
function kubun() {
    document.forms[0].PATTERN2_PRINT_BIRTHDAY.disabled = document.forms[0].PATTERN1.checked;
}
function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
    
function AllClearList(OptionList, TitleName) 
{
        attribute = document.forms[0].COMMI_NAME;
        ClearList(attribute,attribute);
        attribute = document.forms[0].COMMI_SELECTED;
        ClearList(attribute,attribute);
}
function move1(side)
{   
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;
    
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].COMMI_NAME;
        attribute2 = document.forms[0].COMMI_SELECTED;
    } else {
        attribute1 = document.forms[0].COMMI_SELECTED;
        attribute2 = document.forms[0].COMMI_NAME;  
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    //generating new options 
    for (var i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text =  tempa[i];
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
    var current5 = 0;
    var z=0;
    
    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].COMMI_NAME;
        attribute6 = document.forms[0].COMMI_SELECTED;
    } else {
        attribute5 = document.forms[0].COMMI_SELECTED;
        attribute6 = document.forms[0].COMMI_NAME;  
    }

    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
    }

    //generating new options 
    for (var i = 0; i < temp5.length; i++) {
        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[i];
        attribute6.options[i].text =  tempc[i];
    }

    //generating new options
    ClearList(attribute5,attribute5);
}
