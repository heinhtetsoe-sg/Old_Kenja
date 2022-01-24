function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function chkPattern() {
    var dis = true;
    dis = document.forms[0].OUTPUT_PATERN6.checked && (document.forms[0].SEMESTER.value == '3' || document.forms[0].SEMESTER.value == '9') ? false : true;
    if (dis) {
        var tab = document.getElementById("KAIKINTAB");
        backElem = tab;
        parentElem = tab.parentElement;
        parentElem.removeChild(tab);
    } else {
        if (backElem && parentElem) {
            parentElem.appendChild(backElem);
        }
    }
}

function newwin(SERVLET_URL, cmd){
    document.forms[0].cmd.value = cmd;
    if (document.forms[0].CLASS_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return;
    }
    if (document.forms[0].SDATE.value == '' || document.forms[0].DATE.value == '') {
        alert("出欠集計日付が未入力です");
        return;
    }
    if (document.forms[0].SDATE.value > document.forms[0].DATE.value) {
        alert("出欠集計日付が不正です");
        return;
    }

    //日付範囲チェック
	var sday  = document.forms[0].SDATE.value.split('/');        //出欠集計日付
	var day   = document.forms[0].DATE.value.split('/');        //出欠集計日付
	var sdate = document.forms[0].YEAR_SDATE.value.split('/');  //学期開始日付
	var edate = document.forms[0].YEAR_EDATE.value.split('/');  //学期終了日付

	if((new Date(eval(sdate[0]),eval(sdate[1])-1,eval(sdate[2])) > new Date(eval(sday[0]),eval(sday[1])-1,eval(sday[2])))
	   || ((new Date(eval(sday[0]),eval(sday[1])-1,eval(sday[2])) > new Date(eval(edate[0]),eval(edate[1])-1,eval(edate[2])))))
	{
		alert("日付が年度の範囲外です");
		return;
	}

	if((new Date(eval(sdate[0]),eval(sdate[1])-1,eval(sdate[2])) > new Date(eval(day[0]),eval(day[1])-1,eval(day[2])))
	   || ((new Date(eval(day[0]),eval(day[1])-1,eval(day[2])) > new Date(eval(edate[0]),eval(edate[1])-1,eval(edate[2])))))
	{
		alert("日付が年度の範囲外です");
		return;
	}

    if (document.forms[0].TESTKINDCD.value == '') {
        alert("試験が選択されていません");
        return;
    }
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
    document.forms[0].action = SERVLET_URL +"/KNJD";
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
    attribute = document.forms[0].CLASS_NAME;
    ClearList(attribute,attribute);
    attribute = document.forms[0].CLASS_SELECTED;
    ClearList(attribute,attribute);
}
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
    
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

    //assign new values to arrays
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
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = document.forms[0].CLASS_SELECTED;
    }
    else
    {  
        attribute5 = document.forms[0].CLASS_SELECTED;
        attribute6 = document.forms[0].CLASS_NAME;  
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
    }

    //generating new options
    ClearList(attribute5,attribute5);

}
