function btn_submit(cmd) {

    attribute4 = document.forms[0].selectleft2;
    attribute4.value = "";
    sep2 = "";

    attribute3 = document.forms[0].selectleft;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].category_name.length; i++)
    {  
        document.forms[0].category_name.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].category_selected.length; i++)
    {  
        document.forms[0].category_selected.options[i].selected = 1;
        attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value;
        sep = ",";
    }
    //NO005
    for (var i = 0; i < document.forms[0].due_name.length; i++)
    {  
        document.forms[0].due_name.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].due_selected.length; i++)
    {  
        document.forms[0].due_selected.options[i].selected = 1;
        attribute4.value = attribute4.value + sep2 + document.forms[0].due_selected.options[i].value;
        sep2 = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;

}


function newwin(SERVLET_URL){

    for (var i = 0; i < document.forms[0].category_name.length; i++)
    {  
        document.forms[0].category_name.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].category_selected.length; i++)
    {  
        document.forms[0].category_selected.options[i].selected = 1;
    }
    //NO005
    for (var i = 0; i < document.forms[0].due_name.length; i++)
    {  
        document.forms[0].due_name.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].due_selected.length; i++)
    {  
        document.forms[0].due_selected.options[i].selected = 1;
    }
    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJP";
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
        //NO005
        attribute2 = document.forms[0].due_name;
        ClearList(attribute2,attribute2);
        attribute2 = document.forms[0].due_selected;
        ClearList(attribute2,attribute2);
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
    
    if (side == "left")
    {  
        attribute1 = document.forms[0].category_name;
        attribute2 = document.forms[0].category_selected;
    }
    else
    {  
        attribute1 = document.forms[0].category_selected;
        attribute2 = document.forms[0].category_name;  
    }
    
    for (var i = 0; i < attribute2.length; i++)
    {  
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        if (chdt == 1){
            tempaa[y] = String(attribute2.options[i].value).substring(9,19)+","+y;
        }else {
            tempaa[y] = String(attribute2.options[i].value)+","+y;
        }
    }

    for (var i = 0; i < attribute1.length; i++)
    {   
        if ( attribute1.options[i].selected )
        {  
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            if (chdt == 1){
                tempaa[y] = String(attribute1.options[i].value).substring(9,19)+","+y;
            }else {
                tempaa[y] = String(attribute1.options[i].value)+","+y;
            }
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
function moves(sides,chdt)
{   
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    if (sides == "left")
    {  
        attribute5 = document.forms[0].category_name;
        attribute6 = document.forms[0].category_selected;
    }
    else
    {  
        attribute5 = document.forms[0].category_selected;
        attribute6 = document.forms[0].category_name;  
    }

    for (var i = 0; i < attribute6.length; i++)
    {  
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        if (chdt == 1){
            tempaa[z] = String(attribute6.options[i].value).substring(9,19)+","+z;
        }else {
            tempaa[z] = String(attribute6.options[i].value)+","+z;
        }
    }

    for (var i = 0; i < attribute5.length; i++)
    {   
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        if (chdt == 1){
            tempaa[z] = String(attribute5.options[i].value).substring(9,19)+","+z;
        }else {
            tempaa[z] = String(attribute5.options[i].value)+","+z;
        }
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
function duemove(side,chdt)
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
        attribute1 = document.forms[0].due_name;
        attribute2 = document.forms[0].due_selected;
    }
    else
    {  
        attribute1 = document.forms[0].due_selected;
        attribute2 = document.forms[0].due_name;  
    }
    
    for (var i = 0; i < attribute2.length; i++)
    {  
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        if (chdt == 1){
            tempaa[y] = String(attribute2.options[i].value).substring(11,22)+","+y;
        }else {
            tempaa[y] = String(attribute2.options[i].value)+","+y;
        }
    }

    for (var i = 0; i < attribute1.length; i++)
    {   
        if ( attribute1.options[i].selected )
        {  
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            if (chdt == 1){
                tempaa[y] = String(attribute1.options[i].value).substring(11,22)+","+y;
            }else {
                tempaa[y] = String(attribute1.options[i].value)+","+y;
            }
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
    
    btn_submit('change_class');
}
function duemoves(sides,chdt)
{   
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    if (sides == "left")
    {  
        attribute5 = document.forms[0].due_name;
        attribute6 = document.forms[0].due_selected;
    }
    else
    {  
        attribute5 = document.forms[0].due_selected;
        attribute6 = document.forms[0].due_name;  
    }

    
    for (var i = 0; i < attribute6.length; i++)
    {  
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        if (chdt == 1){
            tempaa[z] = String(attribute6.options[i].value).substring(11,22)+","+z;
        }else {
            tempaa[z] = String(attribute6.options[i].value)+","+z;
        }
    }

    for (var i = 0; i < attribute5.length; i++)
    {   
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        if (chdt == 1){
            tempaa[z] = String(attribute5.options[i].value).substring(11,22)+","+z;
        }else {
            tempaa[z] = String(attribute5.options[i].value)+","+z;
        }
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

    //generating new options
    ClearList(attribute5,attribute5);

    btn_submit('change_class');

}
