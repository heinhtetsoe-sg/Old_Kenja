function btn_submit(cmd) {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].ITIRAN.length; i++) {
        document.forms[0].ITIRAN.options[i].selected = 0;
    }
    for (var i = 0; i < document.forms[0].SELECTED_DATA.length; i++) {
        document.forms[0].SELECTED_DATA.options[i].selected = 1;
        attribute3.value = attribute3.value + sep + document.forms[0].SELECTED_DATA.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){
    var s_date = document.forms[0].S_DATE.value; //入力された開始日
    var e_date = document.forms[0].E_DATE.value; //入力された終了日
    var gakki_sdate = document.forms[0].GAKKI_SDATE.value; //チェック用の学期開始日
    var gakki_edate = document.forms[0].GAKKI_EDATE.value; //チェック用の学期終了日
    var gakki_name  = document.forms[0].SEMESTERNAME.value; //選ばれている学期名

    if (document.forms[0].SELECTED_DATA.length == 0) {
        alert('{rval MSG916}');
        return false;
    }

    if (document.forms[0].S_DATE.value == '') {
        alert('開始日を指定して下さい。');
        return false;
    }
    if (document.forms[0].E_DATE.value == '') {
        alert('終了日を指定して下さい。');
        return false;
    }
    if (s_date > e_date) {
        alert("日付の大小が不正です。");
        return false;
    }
    if ((gakki_sdate > s_date) || (gakki_edate < e_date)) {
        alert("指定範囲が不正です。\n" + gakki_name + "：" + gakki_sdate + "～" + gakki_edate);
        return false;
    }

    for (var i = 0; i < document.forms[0].ITIRAN.length; i++) {
        document.forms[0].ITIRAN.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].SELECTED_DATA.length; i++) {
        document.forms[0].SELECTED_DATA.options[i].selected = 1;
    }
        action = document.forms[0].action;
        target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
        document.forms[0].action = SERVLET_URL +"/KNJC";
        document.forms[0].target = "_blank";
        document.forms[0].submit();

        document.forms[0].action = action;
        document.forms[0].target = target;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
        attribute = document.forms[0].ITIRAN;
        ClearList(attribute,attribute);
        attribute = document.forms[0].SELECTED_DATA;
        ClearList(attribute,attribute);
}

//クラス選択／取消（一部）
function move1(side) {
    var temp1  = new Array();
    var temp2  = new Array();
    var tempa  = new Array();
    var tempb  = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left")
    {
        attribute1 = document.forms[0].ITIRAN;
        attribute2 = document.forms[0].SELECTED_DATA;
    }
    else
    {
        attribute1 = document.forms[0].SELECTED_DATA;
        attribute2 = document.forms[0].ITIRAN;
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++)
    {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++)
    {
        if ( attribute1.options[i].selected )
        {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value+","+y;
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
//        attribute2.options[i] = new Option();
//        attribute2.options[i].value = temp1[i];
//        attribute2.options[i].text =  tempa[i];
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

//クラス選択／取消（全部）
function moves(sides)
{
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left")
    {
        attribute5 = document.forms[0].ITIRAN;
        attribute6 = document.forms[0].SELECTED_DATA;
    }
    else
    {
        attribute5 = document.forms[0].SELECTED_DATA;
        attribute6 = document.forms[0].ITIRAN;
    }


    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++)
    {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++)
    {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value+","+z;
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
//        attribute6.options[i] = new Option();
//        attribute6.options[i].value = temp5[i];
//        attribute6.options[i].text =  tempc[i];
    }

    //generating new options
    ClearList(attribute5,attribute5);

}

