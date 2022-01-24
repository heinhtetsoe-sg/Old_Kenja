function btn_submit(cmd) {

    if (cmd == "csv"){
        //クラス
        if (document.forms[0].CLASS_SELECTED.length == 0)
        {
            alert('{rval MSG916}');
            return;
        }
        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++)
        {  
            document.forms[0].CLASS_NAME.options[i].selected = 0;
        }
        for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++)
        {  
            document.forms[0].CLASS_SELECTED.options[i].selected = 1;
            attribute3.value = attribute3.value + sep + document.forms[0].CLASS_SELECTED.options[i].value;
            sep = "','";
        }
        //教科
        if (document.forms[0].CLASSCD_SELECTED.length == 0)
        {
            alert('教科を選択して下さい。');
            return;
        }
        attribute3 = document.forms[0].selectdata2;
        attribute3.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].CLASSCD_NAME.length; i++)
        {  
            document.forms[0].CLASSCD_NAME.options[i].selected = 0;
        }
        for (var i = 0; i < document.forms[0].CLASSCD_SELECTED.length; i++)
        {  
            document.forms[0].CLASSCD_SELECTED.options[i].selected = 1;
            attribute3.value = attribute3.value + sep + document.forms[0].CLASSCD_SELECTED.options[i].value;
            sep = "','";
        }
        if (!confirm('{rval MSG101}')){
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
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

function move1(side)
{   
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();    // 2004/01/26
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;
    
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left")
    {  
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    }
    else
    {  
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++)
    {  
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y; // 2004/01/26
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++)
    {   
        if ( attribute1.options[i].selected )
        {  
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            tempaa[y] = attribute1.options[i].value+","+y; // 2004/01/26
        }
        else
        {  
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();    // 2004/01/26

    //generating new options // 2004/01/26
    for (var i = 0; i < temp1.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
//        attribute2.options[i] = new Option();
//        attribute2.options[i].value = temp1[i];
//        attribute2.options[i].text =  tempa[i];
    }

    //generating new options
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
    var tempaa = new Array();    // 2004/01/26
    var current5 = 0;
    var z=0;
    
    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left")
    {  
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = document.forms[0].CLASS_SELECTED;
    }
    else
    {  
        attribute5 = document.forms[0].CLASS_SELECTED;
        attribute6 = document.forms[0].CLASS_NAME;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++)
    {  
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z; // 2004/01/26
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++)
    {   
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = attribute5.options[i].value+","+z; // 2004/01/26
    }

    tempaa.sort();    // 2004/01/26

    //generating new options // 2004/01/26
    for (var i = 0; i < temp5.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
//        attribute6.options[i] = new Option();
//        attribute6.options[i].value = temp5[i];
//        attribute6.options[i].text =  tempc[i];
    }

    //generating new options
    ClearList(attribute5,attribute5);

}
//教科
function move2(side)
{   
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();    // 2004/01/26
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;
    
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left")
    {  
        attribute1 = document.forms[0].CLASSCD_NAME;
        attribute2 = document.forms[0].CLASSCD_SELECTED;
    }
    else
    {  
        attribute1 = document.forms[0].CLASSCD_SELECTED;
        attribute2 = document.forms[0].CLASSCD_NAME;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++)
    {  
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y; // 2004/01/26
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++)
    {   
        if ( attribute1.options[i].selected )
        {  
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            tempaa[y] = attribute1.options[i].value+","+y; // 2004/01/26
        }
        else
        {  
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();    // 2004/01/26

    //generating new options // 2004/01/26
    for (var i = 0; i < temp1.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
//        attribute2.options[i] = new Option();
//        attribute2.options[i].value = temp1[i];
//        attribute2.options[i].text =  tempa[i];
    }

    //generating new options
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

function moves2(sides)
{   
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();    // 2004/01/26
    var current5 = 0;
    var z=0;
    
    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left")
    {  
        attribute5 = document.forms[0].CLASSCD_NAME;
        attribute6 = document.forms[0].CLASSCD_SELECTED;
    }
    else
    {  
        attribute5 = document.forms[0].CLASSCD_SELECTED;
        attribute6 = document.forms[0].CLASSCD_NAME;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++)
    {  
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z; // 2004/01/26
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++)
    {   
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = attribute5.options[i].value+","+z; // 2004/01/26
    }

    tempaa.sort();    // 2004/01/26

    //generating new options // 2004/01/26
    for (var i = 0; i < temp5.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
//        attribute6.options[i] = new Option();
//        attribute6.options[i].value = temp5[i];
//        attribute6.options[i].text =  tempc[i];
    }

    //generating new options
    ClearList(attribute5,attribute5);

}
