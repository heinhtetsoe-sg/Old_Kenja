<!--kanji=漢字-->
<!-- <?php # $RCSfile: knja145Form1.js,v $ ?> -->
<!-- <?php # $Revision: 56585 $ ?> -->
<!-- <?php # $Date: 2017-10-22 21:47:53 +0900 (日, 22 10 2017) $ ?> -->

function btn_submit(cmd) {

        attribute3 = document.forms[0].selectleft;
        selectleftval = document.forms[0].selectleftval;
        attribute3.value = "";
        selectleftval.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].category_name.length; i++)
        {  
            document.forms[0].category_name.options[i].selected = 0;
        }

        for (var i = 0; i < document.forms[0].category_selected.length; i++)
        {  
            document.forms[0].category_selected.options[i].selected = 1;
            attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value;
            selectleftval.value = selectleftval.value + sep + document.forms[0].category_selected.options[i].text;
            sep = ",";
        }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

    if (document.forms[0].category_selected.length == 0)
    {
        alert('{rval MSG916}');
        return;
    }
    if (document.forms[0].TERM_SDATE.value == '' || document.forms[0].TERM_EDATE.value == '')
    {
        alert('有効期限を指定して下さい。');
        return;
    }

        for (var i = 0; i < document.forms[0].category_name.length; i++)
        {  
            document.forms[0].category_name.options[i].selected = 0;
        }

        for (var i = 0; i < document.forms[0].category_selected.length; i++)
        {  
            document.forms[0].category_selected.options[i].selected = 1;
        }

        action = document.forms[0].action;
        target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
        document.forms[0].action = SERVLET_URL +"/KNJA";
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
    
    //assign what select attribute treat as attribute1 and attribute2
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

    
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++)
    {  
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        if (chdt == 1){
            tempaa[y] = String(attribute2.options[i].value).substr(String(attribute2.options[i].value).indexOf("-"))+","+y; 
        }else {
            tempaa[y] = String(attribute2.options[i].value)+","+y;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++)
    {   
        if ( attribute1.options[i].selected )
        {  
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            if (chdt == 1){
                tempaa[y] = String(attribute1.options[i].value).substr(String(attribute1.options[i].value).indexOf("-"))+","+y; 
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
    
    //assign what select attribute treat as attribute5 and attribute6
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

    
    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++)
    {  
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        if (chdt == 1){
               tempaa[z] = String(attribute6.options[i].value).substr(String(attribute6.options[i].value).indexOf("-"))+","+z; 
        }else {
            tempaa[z] = String(attribute6.options[i].value)+","+z;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++)
    {   
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        if (chdt == 1){
               tempaa[z] = String(attribute5.options[i].value).substr(String(attribute5.options[i].value).indexOf("-"))+","+z; 
        }else {
            tempaa[z] = String(attribute5.options[i].value)+","+z;
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
    ClearList(attribute5,attribute5);

}
