function btn_submit(cmd) {
    if (cmd == "csv") {
        if (validate() == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


function validate() {
    var executedate = document.forms[0].EXECUTEDATE.value
    var sdate = document.forms[0].SDATE.value
    var edate = document.forms[0].EDATE.value
    if (sdate > executedate || edate < executedate) {
        alert('{rval MSG916}' + "\n対象日付は選択した年度・学期の範囲ではありません。");
        return false;
    }

    if (document.forms[0].category_selected.length == 0) {
        alert('{rval MSG916}');
        return false;
    }
    if (document.forms[0].EXECUTEDATE.value == '') {
        alert("対象日付が未入力です。");
        return false;
    }
    attribute3 = document.forms[0].selectData;
    attribute3.value = "";
    sep = "";
    var count = 0;
    for (var i = 0; i < document.forms[0].category_selected.length; i++) {
        if (document.forms[0].OUT_DIV[1].checked && document.forms[0].KIJIKU_CHAIRCD.value == document.forms[0].category_selected.options[i].value) {
        } else {
            count += 1;
        }
        attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value;
        sep = ",";
        if (document.forms[0].OUT_DIV[1].checked && count > 29) {
            alert('{rval MSG914}' + "\n対象講座は基軸講座を含む30講座までしか選択できません。");
            return false;
        } else if (count > 30) {
            alert('{rval MSG914}' + "\n対象講座は30講座までしか選択できません。");
            return false;
        }
        document.forms[0].category_selected.options[i].selected = 1;
    }
    return true;
}

function newwin(SERVLET_URL){
    if (validate() == false) {
        return false;
    }

    for (var i = 0; i < document.forms[0].category_name.length; i++) {
        document.forms[0].category_name.options[i].selected = 0;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJB";
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
    var comma = "|_|_|";
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
    if (side == "left") {
        attribute1 = document.forms[0].category_name;
        attribute2 = document.forms[0].category_selected;
    } else {
        attribute1 = document.forms[0].category_selected;
        attribute2 = document.forms[0].category_name;
    }


    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].text) + comma + y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = String(attribute1.options[i].text) + comma + y;
        } else {
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
        var tmp = val.split(comma);

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
function moves(sides)
{
    var comma = "|_|_|";
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();    // 2004/01/23
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
        tempaa[z] = String(attribute6.options[i].text) + comma + z; // 2004/01/23
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++)
    {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = String(attribute5.options[i].text) + comma + z; // 2004/01/23
    }

    tempaa.sort();    // 2004/01/23

    //generating new options // 2004/01/23
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(comma);

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5,attribute5);
}


