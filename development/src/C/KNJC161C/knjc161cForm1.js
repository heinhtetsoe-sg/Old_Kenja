function btn_submit(cmd) {
    if (cmd == "csv") {
        if (document.forms[0].CLASS_SELECTED.length == 0) {
            alert('{rval MSG916}');
            return;
        }

        if (document.forms[0].KENSUU.value < 1) {
            alert("出力件数を指定して下さい。");
            return;
        }

        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
            document.forms[0].CLASS_NAME.options[i].selected = 0;
        }
        for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
            document.forms[0].CLASS_SELECTED.options[i].selected = 1;
            attribute3.value = attribute3.value + sep + document.forms[0].CLASS_SELECTED.options[i].value;
            sep = ",";
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

    if (document.forms[0].CLASS_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return;
    }

    if (document.forms[0].GRADE == "") {
        alert('{rval MSG310}' + '学年を指定してください。');
        return;
    }

    if (document.forms[0].DATE == "") {
        alert('{rval MSG902}');
        return;
    }

    var day1 = convDate(document.forms[0].DATE.value);

    var dayMinStr = document.forms[0].CTRL_YEAR.value + "/04/01";
    var dayMaxStr = (parseInt(document.forms[0].CTRL_YEAR.value) + 1) + "/03/31";
    var dayMinChk = convDate(dayMinStr);
    var dayMaxChk = convDate(dayMaxStr);
    if (dayMinChk.getTime() - day1.getTime() > 0) {
        alert('{rval MSG916}' + "当年度以前の日付は指定できません。");
        obj1.focus();
        return false;
    }

    if (dayMaxChk.getTime() - day1.getTime() < 0) {
        alert('{rval MSG916}' + "当年度以後の日付は指定できません。");
        obj1.focus();
        return false;
    }

    var strtChkStr = calcWeekDate(document.forms[0].DATE.value, "START");
    document.forms[0].CALCD_START_DATE.value = dFormatM1(convDate(strtChkStr));
    var endChkStr = calcWeekDate(document.forms[0].DATE.value, "END");
    document.forms[0].CALCD_END_DATE.value = dFormatM1(convDate(endChkStr));

    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
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

function convDate(strDate)
{
    var fixDate = strDate.replace('-', '/');
    return new Date(fixDate);
}

function calcWeekDate(sDate, flg)
{
    var fixBDate = sDate.replace('-', '/');
    var dObj = new Date(fixBDate);
    var wDay = dObj.getDay();
    var pDay = 0;
    if (flg == "START") {
        pDay = -(wDay-1);
    } else if (flg == "END") {
        pDay = (7-wDay);
    }

    return getDate(fixBDate, pDay);
}

function getDate(bDate, pday)
{
    var date = new Date(bDate);
    date.setDate(date.getDate() + pday);

    return dFormat(date);
}

function dFormat(dateObj) {
    var year  = dateObj.getFullYear();
    var month = dateObj.getMonth() + 1;
    var day   = dateObj.getDate();
    if (parseInt(month) < 10) {
        month = "0" + month;
    }
    if (parseInt(day) < 10) {
        day = "0" + day;
    }

    return String(year) + "/" + String(month) + "/" + String(day);
}

function dFormatM1(dateObj) {
    dateObj.setDate(dateObj.getDate() - 1);
    
    var year  = dateObj.getFullYear();
    var month = dateObj.getMonth() + 1;
    var day   = dateObj.getDate();
    if (parseInt(month) < 10) {
        month = "0" + month;
    }
    if (parseInt(day) < 10) {
        day = "0" + day;
    }

    return String(year) + "/" + String(month) + "/" + String(day);
}

function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName)
{
        attribute = document.forms[0].CLASS_NAME;
        ClearList(attribute,attribute);
        attribute = document.forms[0].CLASS_SELECTED;
        ClearList(attribute,attribute);
}

function move(side)
{   
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();   // 2004/01/23
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;
    
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "rightall")
    {  
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;
    }
    else
    {  
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++)
    {  
        temp1[current2] = attribute2.options[i].value;
        tempa[current2] = attribute2.options[i].text;
        tempaa[current2] = attribute2.options[i].value+","+current2; // 2004/01/23
        current2++;
    }

    if (side == "rightall" || side == "leftall")
    {  
        for (var i = 0; i < attribute1.length; i++)
        {  
            attribute1.options[i].selected = 1;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++)
    {   
        if ( attribute1.options[i].selected )
        {  
            temp1[current2] = attribute1.options[i].value;
            tempa[current2] = attribute1.options[i].text; 
            tempaa[current2] = attribute1.options[i].value+","+current2; // 2004/01/23
            current2++;
        }
        else
        {  
            temp2[current1] = attribute1.options[i].value; 
            tempb[current1] = attribute1.options[i].text;
            current1++;
        }
    }

    ClearList(attribute2,attribute2);

    tempaa.sort();

    for (var i = 0; i < current2; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }
    attribute2.length = current2;

    ClearList(attribute1,attribute1);
    if (current1>0)
    {   
        for (var i = 0; i < current1; i++)
        {   
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }
    attribute1.length = current1;

}
