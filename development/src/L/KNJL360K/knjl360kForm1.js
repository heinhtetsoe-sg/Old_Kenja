function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//印刷
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

    //NO002
    for (var j = 0; j < document.forms[0].R_COURSE.length; j++)
    {
        document.forms[0].R_COURSE.options[j].selected = 0;
    }
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var j = 0; j < document.forms[0].L_COURSE.length; j++)
    {
        document.forms[0].L_COURSE.options[j].selected = 1;
        attribute3.value = attribute3.value + sep + document.forms[0].L_COURSE.options[j].value;
        sep = ",";
    }

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//選択ソートを保管---NO002
function sort_val(output_val){
    var jhflg = document.forms[0].JHFLG.value;
    if (jhflg == 1) return false;
    if (output_val == 1 || output_val == 4 || output_val == 7) {
        document.forms[0].L_COURSE.disabled = false;
        document.forms[0].R_COURSE.disabled = false;
        document.forms[0].sel_add_all.disabled = false;
        document.forms[0].sel_add.disabled = false;
        document.forms[0].sel_del.disabled = false;
        document.forms[0].sel_del_all.disabled = false;
        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].L_COURSE.length; i++)
        {
            attribute3.value = attribute3.value + sep + document.forms[0].L_COURSE.options[i].value;
            sep = ",";
        }
    }
}
//NO002
function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}
//NO002
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

    if (side == "right" || side == "sel_del_all")
    {
        attribute1 = document.forms[0].L_COURSE;
        attribute2 = document.forms[0].R_COURSE;
    }
    else
    {
        attribute1 = document.forms[0].R_COURSE;
        attribute2 = document.forms[0].L_COURSE;
    }


    for (var i = 0; i < attribute2.length; i++)
    {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
    }

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
    for (var i = 0; i < temp1.length; i++)
    {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text =  tempa[i];
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
//NO002
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

        if (side == "left")
        {  
            attribute1 = document.forms[0].L_COURSE;
            attribute2 = document.forms[0].R_COURSE;
        }
        else
        {  
            attribute1 = document.forms[0].R_COURSE;
            attribute2 = document.forms[0].L_COURSE;  
        }

        for (var i = 0; i < attribute2.length; i++)
        {  
            y=current1++
            temp1[y] = attribute2.options[i].value;
            tempa[y] = attribute2.options[i].text;
        }

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
        for (var i = 0; i < temp1.length; i++)
        {  
            attribute2.options[i] = new Option();
            attribute2.options[i].value = temp1[i];
            attribute2.options[i].text =  tempa[i];
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
