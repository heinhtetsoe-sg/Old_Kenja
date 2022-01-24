// kanji=漢字

function btn_submit(cmd) {

    
    for (ids = 3; ids <= 6; ids++) {
        for (i = 0; i < document.forms[0]["CATEGORY_NAME"+ids].length; i++) {
            document.forms[0]["CATEGORY_NAME"+ids].options[i].selected = 0;
        }
        var categoryObj = document.forms[0]["CATEGORY_SELECTED"+ids];
        var hidCategoryObj = document.forms[0]["HID_CATEGORY_SELECTED"+ids];
        var sep = '';
        for (i = 0; i < categoryObj.length; i++) {
            categoryObj.options[i].selected = 1;
            hidCategoryObj.value = hidCategoryObj.value + sep + categoryObj.options[i].value;
            sep = ',';
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList) 
{
    OptionList.length = 0;
}
    
function AllClearList() 
{
    attribute = document.forms[0].CATEGORY_NAME;
    ClearList(attribute);
    attribute = document.forms[0].CATEGORY_SELECTED;
    ClearList(attribute);
}

///////////////////
function moven(side, student, ids)
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
        attribute1 = document.forms[0]["CATEGORY_NAME"+ids];
        attribute2 = document.forms[0]["CATEGORY_SELECTED"+ids];
    } else {
        attribute1 = document.forms[0]["CATEGORY_SELECTED"+ids];
        attribute2 = document.forms[0]["CATEGORY_NAME"+ids];
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        if(student){
            tempaa[y] = String(attribute2.options[i].text).substring(9,12)+","+y;
        } else {
            tempaa[y] = attribute2.options[i].value+","+y;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            if(student){
                tempaa[y] = String(attribute1.options[i].text).substring(9,12)+","+y;
            } else {
                tempaa[y] = attribute1.options[i].value+","+y;
            }
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp1.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1);
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

function movesn(sides, student, ids)
{   
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left")
    {  
        attribute5 = document.forms[0]["CATEGORY_NAME"+ids];
        attribute6 = document.forms[0]["CATEGORY_SELECTED"+ids];
    }
    else
    {  
        attribute5 = document.forms[0]["CATEGORY_SELECTED"+ids];
        attribute6 = document.for[0]["CATEGORY_NAME"+ids];
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++)
    {  
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        if(student){
            tempaa[z] = String(attribute6.options[i].text).substring(9,12)+","+z;
        } else {
            tempaa[z] = attribute6.options[i].value+","+z;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++)
    {   
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        if(student){
            tempaa[z] = String(attribute5.options[i].text).substring(9,12)+","+z;
        } else {
            tempaa[z] = attribute5.options[i].value+","+z;
        }
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp5.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5);
}
