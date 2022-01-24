function btn_submit(cmd)
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL)
{
    if (document.forms[0].CATEGORY_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return false;
    }

    var obj1 = document.forms[0].SDATE;
    if (obj1.value == '') {
        alert('{rval MSG902}' + "日付が不正です。");
        obj1.focus();
        return false;
    }
    var obj2 = document.forms[0].EDATE;
    if (obj2.value == '') {
        alert('{rval MSG902}' + "日付が不正です。");
        obj2.focus();
        return false;
    }
    var day1 = convDate(document.forms[0].SDATE.value);
    var day2 = convDate(document.forms[0].EDATE.value);
    if (day1.getTime() > day2.getTime()) {
        alert('{rval MSG916}' + "開始日が終了日を超えています。");
        obj2.focus();
        return false;
    }
    if (day2.getTime() - day1.getTime() > 31536000000) {
        alert('{rval MSG916}' + "1年以上の期間の出力はできません。");
        return false;
    }
    var dayMinStr = document.forms[0].YEAR.value + "/04/01";
    var dayMaxStr = (parseInt(document.forms[0].YEAR.value) + 1) + "/03/31";
    var dayMinChk = convDate(dayMinStr);
    var dayMaxChk = convDate(dayMaxStr);
    if (dayMinChk.getTime() - day1.getTime() > 0) {
        alert('{rval MSG916}' + "当年度以前の日付は指定できません。");
        obj1.focus();
        return false;
    }

    if (dayMaxChk.getTime() - day2.getTime() < 0) {
        alert('{rval MSG916}' + "当年度以後の日付は指定できません。");
        obj1.focus();
        return false;
    }
    var strtChkStr = calcWeekDate(document.forms[0].SDATE.value, "START");
    var strtChkObj = convDate(strtChkStr);
    var endChkStr = calcWeekDate(document.forms[0].EDATE.value, "END");
    var endChkObj = convDate(endChkStr);

    //年度を超えないよう、日付を指定する。
    if (dayMinChk.getTime() - strtChkObj.getTime() > 0) {
        document.forms[0].CALCD_START_DATE.value = dayMinStr;
    } else {
        document.forms[0].CALCD_START_DATE.value = strtChkStr;
    }

    //年度を超えないよう、日付を指定する。
    if (dayMaxChk.getTime() - endChkObj.getTime() < 0) {
        document.forms[0].CALCD_END_DATE.value = dayMaxStr;
    } else {
        document.forms[0].CALCD_END_DATE.value = endChkStr;
    }

    for (var i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
        document.forms[0].CATEGORY_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        document.forms[0].CATEGORY_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    //url = location.hostname;
    //document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
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
        pDay = 6-wDay;
    }

    return getDate(fixBDate, pDay);
}

function getDate(bDate, pday)
{
    var date = new Date(bDate);
    date.setDate(date.getDate() + pday);

    var year  = date.getFullYear();
    var month = date.getMonth() + 1;
    var day   = date.getDate();
    if (parseInt(month) < 10) {
        month = "0" + month;
    }
    if (parseInt(day) < 10) {
        day = "0" + day;
    }

    return String(year) + "/" + String(month) + "/" + String(day);
}

function convDate(strDate)
{
    var fixDate = strDate.replace('-', '/');
    return new Date(fixDate);
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
    
//NO008 ↓
function move1(side)
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
    
    if (side == "left") {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;
    }

    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value+","+y;
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    ClearList(attribute1,attribute1);
    if (temp2.length>0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }
}

function moves(sides)
{
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    if (sides == "left") {
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;
    }

    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value+","+z;
    }

    tempaa.sort();

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    ClearList(attribute5,attribute5);

}

