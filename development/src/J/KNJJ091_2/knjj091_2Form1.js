function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit() {

    if (document.forms[0].COMMITTEE_FLG.value == "" && document.forms[0].COMMITTEECD.value == ""){
        alert('{rval MSG301}\n'+'　　　（委員会区分または委員会）');
        return true;
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";

    if (document.forms[0].STUDENT_SELECTED.length == 0 && document.forms[0].STUDENT_NAME.length == 0) {
        alert('{rval MSG916}');
        return false;
    }

    for (var i = 0; i < document.forms[0].STUDENT_SELECTED.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].STUDENT_SELECTED.options[i].value;
        sep = ",";
    }

    if (attribute3.value == "") {
        alert('{rval MSG304}');
        return false;
    }

   document.forms[0].cmd.value = 'insert';
   document.forms[0].submit();
   return false;
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
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
    
    if (side == "left")
    {  
        attribute1 = document.forms[0].STUDENT_NAME;
        attribute2 = document.forms[0].STUDENT_SELECTED;
    }
    else
    {  
        attribute1 = document.forms[0].STUDENT_SELECTED;
        attribute2 = document.forms[0].STUDENT_NAME;  
    }

    for (var i = 0; i < attribute2.length; i++)
    {  
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].text).substring(0,3)+","+y;
    }

    for (var i = 0; i < attribute1.length; i++)
    {   
        if ( attribute1.options[i].selected )
        {  
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            tempaa[y] = String(attribute1.options[i].text).substring(0,3)+","+y;
        }
        else
        {  
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    for (var i = 0; i < temp1.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    ClearList(attribute1,attribute1);
    if (temp2.length>0)
    {    
        for (var i = 0; i < temp2.length; i++)
        {   
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
    
    if (sides == "left")
    {  
        attribute5 = document.forms[0].STUDENT_NAME;
        attribute6 = document.forms[0].STUDENT_SELECTED;
    }
    else
    {  
        attribute5 = document.forms[0].STUDENT_SELECTED;
        attribute6 = document.forms[0].STUDENT_NAME;  
    }

    for (var i = 0; i < attribute6.length; i++)
    {  
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = String(attribute6.options[i].text).substring(0,3)+","+z;
    }

    for (var i = 0; i < attribute5.length; i++)
    {   
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = String(attribute5.options[i].text).substring(0,3)+","+z;
    }

    tempaa.sort();

    for (var i = 0; i < temp5.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    ClearList(attribute5,attribute5);

}
