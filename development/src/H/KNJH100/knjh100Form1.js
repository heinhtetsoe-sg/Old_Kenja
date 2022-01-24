function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

    if (document.forms[0].CLASS_SELECTED.length == 0)
    {
        alert('{rval MSG916}');
        return;
    }
//  if (document.forms[0].HYOSHO.checked==false && document.forms[0].BATSU.checked==false && document.forms[0].SIKAKU.checked==false)
    if (document.forms[0].HYOSHO.checked==false && document.forms[0].BATSU.checked==false)
    {
        alert('{rval MSG916}');
        return;
    }

    select1 = document.forms[0].HS_KUBUN_FROM;
    select2 = document.forms[0].HS_KUBUN_TO;
    wrkindex = 0;

    if ( select1.selectedIndex > select2.selectedIndex )
    {  
        wrkindex = select1.selectedIndex;
        select1.selectedIndex = select2.selectedIndex;
        select2.selectedIndex = wrkindex;
    }

    select3 = document.forms[0].BS_KUBUN_FROM;
    select4 = document.forms[0].BS_KUBUN_TO;
    wrkindex3 = 0;

    if ( select3.selectedIndex > select4.selectedIndex )
    {  
        wrkindex3 = select3.selectedIndex;
        select3.selectedIndex = select4.selectedIndex;
        select4.selectedIndex = wrkindex3;
    }
/*
    select5 = document.forms[0].ST_KUBUN_FROM;
    select6 = document.forms[0].ST_KUBUN_TO;
    wrkindex5 = 0;

    if ( select5.selectedIndex > select6.selectedIndex )
    {  
        wrkindex5 = select5.selectedIndex;
        select5.selectedIndex = select6.selectedIndex;
        select6.selectedIndex = wrkindex5;
    }
*/
    //
    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++)
    {  
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++)
    {  
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

        document.forms[0].action = SERVLET_URL +"/KNJH";
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
        attribute = document.forms[0].category_name;
        ClearList(attribute,attribute);
        attribute = document.forms[0].category_selected;
        ClearList(attribute,attribute);
}

function move1(side)
{   
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();   // 2004/01/26
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

    tempaa.sort();  // 2004/01/26

    //generating new options // 2004/01/26
    for (var i = 0; i < temp1.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
//      attribute2.options[i] = new Option();
//      attribute2.options[i].value = temp1[i];
//      attribute2.options[i].text =  tempa[i];
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
    var tempaa = new Array();   // 2004/01/26
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

    tempaa.sort();  // 2004/01/26

    //generating new options // 2004/01/26
    for (var i = 0; i < temp5.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
//      attribute6.options[i] = new Option();
//      attribute6.options[i].value = temp5[i];
//      attribute6.options[i].text =  tempc[i];
    }

    //generating new options
    ClearList(attribute5,attribute5);

}

function HSUse(obj1)
{
    if(document.forms[0].HYOSHO.checked == true)
    {
        flag1 = false;
    }
    else
    {
        flag1 = true;
    }
    document.forms[0].HS_KUBUN_FROM.disabled = flag1;
    document.forms[0].HS_KUBUN_TO.disabled = flag1;
}
function BSUse(obj2)
{
    if(document.forms[0].BATSU.checked == true)
    {
        flag1 = false;
    }
    else
    {
        flag1 = true;
    }
    document.forms[0].BS_KUBUN_FROM.disabled = flag1;
    document.forms[0].BS_KUBUN_TO.disabled = flag1;
}
/*
function STUse(obj1)
{
    if(document.forms[0].SIKAKU.checked == true)
    {
        flag1 = false;
    }
    else
    {
        flag1 = true;
    }
    document.forms[0].ST_KUBUN_FROM.disabled = flag1;
    document.forms[0].ST_KUBUN_TO.disabled = flag1;
}
*/
