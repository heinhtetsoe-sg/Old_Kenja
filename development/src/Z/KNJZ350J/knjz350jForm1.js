function btn_submit(cmd) {
    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}'))
            return false;
        else
            cmd = "";
    }
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
}

function doSubmit() {
    var attribute1 = document.forms[0].selectdata;
    attribute1.value = "";
    var attribute2 = document.forms[0].selectdata2;
    attribute2.value = "";
    var attribute3 = document.forms[0].selectdata3;
    attribute3.value = "";
    if ((document.forms[0].grade_input.length==0 && document.forms[0].grade_delete.length==0) ||
        (document.forms[0].attend_input.length==0 && document.forms[0].attend_delete.length==0) ||
        (document.forms[0].jview_input.length==0 && document.forms[0].jview_delete.length==0)) {
        alert('{rval MSG916}');
        return false;
    }
    sep = "";
    for (var i = 0; i < document.forms[0].grade_input.length; i++)
    {
        attribute1.value = attribute1.value + sep + document.forms[0].grade_input.options[i].value;
        sep = ",";
    }
    sep = "";
    for (var i = 0; i < document.forms[0].attend_input.length; i++)
    {
        attribute2.value = attribute2.value + sep + document.forms[0].attend_input.options[i].value;
        sep = ",";
    }
    sep = "";
    for (var i = 0; i < document.forms[0].jview_input.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].jview_input.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}


function temp_clear()
{
    ClearList(document.forms[0].sectionyear,document.forms[0].sectionyear);
    ClearList(document.forms[0].sectionmaster,document.forms[0].sectionmaster);
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

//リスト移動（使ってない）
/*
function move2(side, left, right, sort)
{
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var temp  = new Array();
    var tempx = new Array();
    var a = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute1;
    var attribute2;
    var joined_array;
    
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
        y=current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        a[tempa[y]] = temp1[y];
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
                a[tempa[y]] = temp1[y];
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
            a[tempa[y]] = temp1[y];
        }
    }
//    if (sort){
//        //sort
//        tempa = tempa.sort();
//        //generating new options
//        for (var i = 0; i < tempa.length; i++)
//        {
//            alert(a[tempa[i]]);
//            temp1[i] = a[tempa[i]];
//        }
//    }
    if (sort) {
        //sort
        temp1 = temp1.sort();
//        temp  = temp1;
//        joined_array = temp.join("/");
//        joined_array = sort_custom(joined_array, 1);
//        temp = joined_array.split("/");
//        joined_array = tempa.join("/");
//        joined_array = sort_custom(joined_array, 2);
//        tempx = joined_array.split("/");
        //generating new options
        for (var i = 0; i < tempa.length; i++)
        {
            if (tempa[i].length == 2) {
                tempa[i] = "００" + tempa[i];
            }else if (tempa[i].length == 3) {
                tempa[i] = "０" + tempa[i];
            }
        }
        tempa = tempa.sort();
        for (i = 0; i < tempa.length; i++)
        {
            if (tempa[i].match("/'月'/", "")) {
                tempa[i] = tempa[i].replace("/^０/i", "");
                alert("hoge");
            }
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
    if (temp2.length > 0)
    {
        for (var i = 0; i < temp2.length; i++)
        {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }

    var attribute3 = document.forms[0].selectdata1;
    attribute3.value = "";
    var attribute4 = document.forms[0].selectdata2;
    attribute4.value = "";
    sep = "";
    if (left == "grade_input") {
        for (var i = 0; i < document.forms[0][left].length; i++)
        {
            attribute3.value = attribute3.value + sep + document.forms[0][left].options[i].value;
            sep = ",";
        }
    } else if (left == "attend_input") {
        for (var i = 0; i < document.forms[0][left].length; i++)
        {
            attribute4.value = attribute4.value + sep + document.forms[0][left].options[i].value;
            sep = ",";
        }
    }
}
*/

//（使ってない）
function sort_custom(sort_array, kind)
{
    var array = new Array();
    var temp = new Array();
    alert("in function pre : " + kind + " " + sort_array);
    array = sort_array.split("/");
    
    if (kind == 1) {
        for (var i = 0; i < array.length; i++)
        {
            if (array[i] != 13) {
                array[i] = eval(array[i]) - 3;
            }
            if (array[i] <= 0) {
                array[i] = eval(array[i]) + 12;
            }
            if (array[i] < 10) {
                array[i] = "0" + array[i];
            }
        }
        array.sort();
    } else {
        for (var i = 0; i < array.length; i++)
        {
            alert(array[i].slice(-1));
        }
    }
    sort_array = array.join("/");
    alert("in function post : " + kind + " " + sort_array);
    return sort_array;
}
