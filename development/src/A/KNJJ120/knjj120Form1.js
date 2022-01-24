function btn_submit(cmd) {

    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";

    if (cmd == 'change'){
        for (var i = 0; i < document.forms[0].STUDENT_SELECTED.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].STUDENT_SELECTED.options[i].value;
            sep = ",";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit() {

    if (document.forms[0].CLUBCD.value == ""){
        alert('{rval MSG304}\n'+'　　　　（部クラブ）');
        return true;
    }
    if (document.forms[0].DETAIL_DATE.value == ""){
        alert('{rval MSG301}\n'+'　　　　（日付）');
        return true;
    }

	//年度範囲チェック
	var detail_date = document.forms[0].DETAIL_DATE.value.split('/'); //日付
	var chk_sdate = document.forms[0].CHK_SDATE.value.split('/'); //年度開始日
	var chk_edate = document.forms[0].CHK_EDATE.value.split('/'); //年度終了日

	if(new Date(eval(detail_date[0]),eval(detail_date[1])-1,eval(detail_date[2])) >= new Date(eval(chk_sdate[0]),eval(chk_sdate[1])-1,eval(chk_sdate[2])) && 
	   new Date(eval(detail_date[0]),eval(detail_date[1])-1,eval(detail_date[2])) <= new Date(eval(chk_edate[0]),eval(chk_edate[1])-1,eval(chk_edate[2])))
	{
	} else {
		alert("日付が年度範囲外です。");
		return;
	}

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";

    if (document.forms[0].STUDENT_SELECTED.length == 0 && document.forms[0].STUDENT_NAME.length == 0) {
        alert('{rval MSG916}');
        return false;
    }

    for (var i = 0; i < document.forms[0].STUDENT_SELECTED.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].STUDENT_SELECTED.options[i].value;
        sep = ",";
    }

    if (attribute3.value == "") {
        alert('{rval MSG304}');
        return false;
    }

   document.forms[0].cmd.value = 'update';
   document.forms[0].submit();
   return false;
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
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
    
    if (side == "left")
    {  
        attribute1 = document.forms[0].STUDENT_NAME;
        attribute2 = document.forms[0].STUDENT_SELECTED;
    }
    else
    {  
        attribute1 = document.forms[0].STUDENT_SELECTED;
        attribute2 = document.forms[0].STUDENT_NAME;  
    }

    for (var i = 0; i < attribute2.length; i++)
    {  
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

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

    for (var i = 0; i < temp1.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
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

function moves(sides)
{   
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    if (sides == "left")
    {  
        attribute5 = document.forms[0].STUDENT_NAME;
        attribute6 = document.forms[0].STUDENT_SELECTED;
    }
    else
    {  
        attribute5 = document.forms[0].STUDENT_SELECTED;
        attribute6 = document.forms[0].STUDENT_NAME;  
    }

    for (var i = 0; i < attribute6.length; i++)
    {  
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z;
    }

    for (var i = 0; i < attribute5.length; i++)
    {   
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = attribute5.options[i].value+","+z;
    }

    tempaa.sort();

    for (var i = 0; i < temp5.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    ClearList(attribute5,attribute5);

}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
