
function doSubmit() {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].left_select.length==0 && document.forms[0].right_select.length==0) {
        alert('{rval MSG916}');
        return false;
    }
    if (document.forms[0].left_select.length==0) {
        alert('{rval MSG304}');
        return false;
    }
    for (var i = 0; i < document.forms[0].left_select.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = 'subform1_update';
    document.forms[0].submit();
    return false;
}

function ShowMessage()
{
    alert('コースコードが設定されていません。コースマスタメンテを確認してください。');
    parent.location.href='knja090mindex.php?cmd=list';
}

function ShowMessage2()
{
    alert('課程コードまたは学科コードが設定されていません。\n課程マスタメンテ、学科マスタメンテを確認してください。');
    parent.location.href='knja090mindex.php?cmd=list';
}


function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
    
function temp_clear()
{
    ClearList(document.forms[0].classyear,document.forms[0].classyear);
    ClearList(document.forms[0].classmaster,document.forms[0].classmaster);
}

function OnAuthError()
{
    alert('{rval MSG300}');
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
            attribute1 = document.forms[0].left_select;
            attribute2 = document.forms[0].right_select;
        }
        else
        {  
            attribute1 = document.forms[0].right_select;
            attribute2 = document.forms[0].left_select;  
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
    temp1 = temp1.sort();
    tempa = tempa.sort();

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
