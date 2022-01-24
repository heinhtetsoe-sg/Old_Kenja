function btn_submit(cmd) {
    if (cmd=="clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit() {
    if (document.forms[0].LEFTLIST.length==0 && 
       document.forms[0].CENTERLIST.length==0 &&
       document.forms[0].RIGHTLIST.length==0
       ) {
        alert('{rval MSG303}');
        return false;
    }
    
    var data  = "";
    sep = "";
    if (document.forms[0].LEFTLIST.length > 0){
        for (var i = 0; i < document.forms[0].LEFTLIST.length; i++)
        {
            data += sep + document.forms[0].LEFTLIST.options[i].value;
            sep = ",";
        }
    }
    document.forms[0].selectdata.value = data;
    data  = "";
    sep = "";
    if (document.forms[0].CENTERLIST.length > 0){
        for (var i = 0; i < document.forms[0].CENTERLIST.length; i++)
        {
            data += sep + document.forms[0].CENTERLIST.options[i].value;
            sep = ",";
        }
    }
    document.forms[0].selectdata2.value = data;
    data  = "";
    sep = "";
    if (document.forms[0].RIGHTLIST.length > 0){
        for (var i = 0; i < document.forms[0].RIGHTLIST.length; i++)
        {
            data += sep + document.forms[0].RIGHTLIST.options[i].value;
            sep = ",";
        }
    }
    document.forms[0].selectdata3.value = data;
    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;
}
function ClearList(OptionList) {
    OptionList.length = 0;
}

function temp_clear()
{
    ClearList(document.forms[0].LEFTLIST);
    ClearList(document.forms[0].CENTERLIST);
    ClearList(document.forms[0].RIGHTLIST);
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function move3(direction,left_name,right_name,flg)
{
    move2(direction,left_name,right_name, flg);
    document.getElementById("LEFTLIST_NUM").innerHTML = document.forms[0].LEFTLIST.options.length;
    document.getElementById("CENTERLIST_NUM").innerHTML = document.forms[0].CENTERLIST.options.length;
    document.getElementById("RIGHTLIST_NUM").innerHTML = document.forms[0].RIGHTLIST.options.length;
    return;
}

function clickRadio(val){
    if (val == 1){
        document.forms[0].csvfile.disabled =false;
    }else{
        document.forms[0].csvfile.disabled =true;
    }
}

/*
function move2(side, left, right, sort)
{
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var a = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute1;
    var attribute2;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all")
    {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    }
    else
    {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++)
    {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        a[temp1[y]] = tempa[y];
        tempaa[y] = attribute2.options[i].text.substring(0,attribute2.options[i].text.indexOf("("))+","+y;
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
//              spval = attribute1.options[i].text.split("(")
//              tempa[y] = spval;
                a[temp1[y]] = tempa[y];
                tempaa[y] = attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("))+","+y;
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
//          spval = attribute1.options[i].text.split("(")
//          tempa[y] = spval;
            a[temp1[y]] = tempa[y];
            tempaa[y] = attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("))+","+y;
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

    //generating new options
    for (var i = 0; i < temp1.length; i++)
    {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text =  tempa[i];
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

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0][left].length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0][left].options[i].value;
        sep = ",";
    }
}
*/
/*
function move2(side, left, right, sort)
{
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var a = new Array();
    var b = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute1;
    var attribute2;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all")
    {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    }
    else
    {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++)
    {
        y=current1++

        temp1[attribute2.options[i].text.substring(0,attribute2.options[i].text.indexOf("("))] = attribute2.options[i].value;
        tempa[attribute2.options[i].text.substring(0,attribute2.options[i].text.indexOf("("))] = attribute2.options[i].text;
        a[attribute2.options[i].text.substring(0,attribute2.options[i].text.indexOf("("))] = tempa[attribute2.options[i].text.substring(0,attribute2.options[i].text.indexOf("("))];
        b[attribute2.options[i].text.substring(0,attribute2.options[i].text.indexOf("("))] = temp1[attribute2.options[i].text.substring(0,attribute2.options[i].text.indexOf("("))];

        tempaa[y] = attribute2.options[i].text.substring(0,attribute2.options[i].text.indexOf("("));

    }
    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++)
    {
        if (side == "right" || side == "left")
        {
            if ( attribute1.options[i].selected )
            {
                y=current1++
                temp1[attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("))] = attribute1.options[i].value;
                tempa[attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("))] = attribute1.options[i].text;
                a[attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("))] = tempa[attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("))];
                b[attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("))] = temp1[attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("))];

                tempaa[y] = attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("));

            }
            else
            {
                y=current2++
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        } else {

            y=current1++
            temp1[attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("))] = attribute1.options[i].value;
            tempa[attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("))] = attribute1.options[i].text;
            a[attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("))] = tempa[attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("))];
            b[attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("))] = temp1[attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("))];

            tempaa[y] = attribute1.options[i].text.substring(0,attribute1.options[i].text.indexOf("("));

        }
    }
    if (sort){
        //sort
        tempaa = tempaa.sort();
        //generating new options
        for (var i = 0; i < temp1.length; i++)
        {
            tempa[i] = a[tempaa[i]];
            temp1[i] = b[tempaa[i]];
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

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0][left].length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0][left].options[i].value;
        sep = ",";
    }
}
*/
function move2(side, left, right, sort)
{
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var a = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute1;
    var attribute2;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all")
    {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    }
    else
    {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++)
    {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        a[temp1[y]] = tempa[y];
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
                a[temp1[y]] = tempa[y];
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
            a[temp1[y]] = tempa[y];
        }
    }
    if (sort){
        //sort
        temp1 = temp1.sort();
        //generating new options
        for (var i = 0; i < temp1.length; i++)
        {
            tempa[i] = a[temp1[i]];
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

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0][left].length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0][left].options[i].value;
        sep = ",";
    }
}
