function btn_submit(cmd) {
    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    if (cmd == 'list') {
        window.open('knjz232index.php?cmd=sel&init=1','right_frame');
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function doSubmit() {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].classyear.length==0 && document.forms[0].classmaster.length==0) {
        alert('{rval MSG916}');
        return false;
    }
    for (var i = 0; i < document.forms[0].classyear.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].classyear.options[i].value;
        sep = ",";
    }
    //get the number of selected gradingclasses as a grading_flg.
    document.forms[0].grading_flg.value = document.forms[0].classyear.length;

    document.forms[0].cmd.value = 'check';
    document.forms[0].submit();
    return false;
}

function Show_Confirm()
{
    if (!confirm("成績処理がされてます。変更しますか？\n変更する場合は成績処理を再度実行してください。")){
        document.forms[0].record_dat_flg.value = '0';
        document.forms[0].cmd.value = 'sel';
        document.forms[0].submit();
        return false;
    } else {
        document.forms[0].record_dat_flg.value = '1';
        document.forms[0].cmd.value = 'update';
        document.forms[0].submit();
        return false;
    }
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
    
function move(side)
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
    if (side == "right" || side == "sel_del_all")
    {  
        attribute1 = document.forms[0].classyear;
        attribute2 = document.forms[0].classmaster;
    }
    else
    {  
        attribute1 = document.forms[0].classmaster;
        attribute2 = document.forms[0].classyear;  
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
        if (side == "right" || side == "left") 
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
        } else {

            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
        }
    }

    //sort
    if (side == "right")
    {  
       for (var i = 0; i < temp1.length; i++)
       {  
       t1 = temp1[i]; 
       ta = tempa[i]; 
       j = i-1;

       while((j>-1) && (temp1[j]>t1)){
               temp1[j+1] = temp1[j]; 
               tempa[j+1] = tempa[j]; 
               j--;

       }
               temp1[j+1] = t1;
               tempa[j+1] = ta;
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
    
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].classyear.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].classyear.options[i].value;
        sep = ",";
    }
}

function OnAuthError()
{
    alert('{rval MZ0026}');
    closeWin();
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
            attribute1 = document.forms[0].classyear;
            attribute2 = document.forms[0].classmaster;
        }
        else
        {  
            attribute1 = document.forms[0].classmaster;
            attribute2 = document.forms[0].classyear;  
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

        //sort
        if (side == "left")
        {  

            for (var i = 0; i < temp1.length; i++)
            {  
               t1 = temp1[i]; 
               ta = tempa[i]; 
               j = i-1;

            while((j>-1) && (temp1[j]>t1)){
                   temp1[j+1] = temp1[j]; 
                   tempa[j+1] = tempa[j]; 
                   j--;

            }
                   temp1[j+1] = t1;
                   tempa[j+1] = ta;
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
