function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

    if (document.forms[0].SCHOOL_SELECTED.length == 0)
    {
        alert('{rval MSG916}');
        return;
    }


    //
    for (var i = 0; i < document.forms[0].SCHOOL_NAME.length; i++)
    {  
        document.forms[0].SCHOOL_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].SCHOOL_SELECTED.length; i++)
    {  
        document.forms[0].SCHOOL_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJL";
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
        attribute = document.forms[0].SCHOOL_NAME;
        ClearList(attribute,attribute);
        attribute = document.forms[0].SCHOOL_SELECTED;
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
    if (side == "left")
    {  
        attribute1 = document.forms[0].SCHOOL_NAME;
        attribute2 = document.forms[0].SCHOOL_SELECTED;
    }
    else
    {  
        attribute1 = document.forms[0].SCHOOL_SELECTED;
        attribute2 = document.forms[0].SCHOOL_NAME;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++)
    {  
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++)
    {   
        if ( attribute1.options[i].selected )
        {  
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
        }
        else
        {  
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    //generating new options 
    for (var i = 0; i < temp1.length; i++)
    {  
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text =  tempa[i];
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
    var current5 = 0;
    var z=0;
    
    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left")
    {  
        attribute5 = document.forms[0].SCHOOL_NAME;
        attribute6 = document.forms[0].SCHOOL_SELECTED;
    }
    else
    {  
        attribute5 = document.forms[0].SCHOOL_SELECTED;
        attribute6 = document.forms[0].SCHOOL_NAME;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++)
    {  
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++)
    {   
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
    }

    //generating new options 
    for (var i = 0; i < temp5.length; i++)
    {  
        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[i];
        attribute6.options[i].text =  tempc[i];
    }

    //generating new options
    ClearList(attribute5,attribute5);

}
