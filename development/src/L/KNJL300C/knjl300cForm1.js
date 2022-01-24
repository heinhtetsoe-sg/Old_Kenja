function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

	if (document.forms[0].TESTDIV.value == ""){
		alert("入試区分を指定して下さい");
		return;
	}

	if (document.forms[0].EXAM_TYPE.value == ""){
		alert("受験型を指定して下さい");
		return;
	}

    if (document.forms[0].CATEGORY_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return;
    }
    for (var i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
        document.forms[0].CATEGORY_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        document.forms[0].CATEGORY_SELECTED.options[i].selected = 1;
    }

    //受付番号の大小チェック
    if (!checkedReceptNo()) {
        return;
    }

    //受付番号のチェック
	var no   = document.forms[0].CATEGORY_SELECTED.value.split('-');        //印刷範囲
	var no_from  = document.forms[0].RECEPTNO_FROM.value; 				    //印刷範囲(入力開始)
	var no_to  = document.forms[0].RECEPTNO_TO.value; 				    //印刷範囲(入力終了)
    var rno2 = no[1].replace(/^0+/, "");
    var rno3 = no[2].replace(/^0+/, "");
	var no2  = parseInt(rno2);									    //整数変換:範囲開始番号
	var no3  = parseInt(rno3);									    //整数変換:範囲最終番号
	var sns ;
	var ens ;

	if(document.forms[0].CATEGORY_SELECTED.length == 1){
		//終了番号のみ入力の場合
		if((document.forms[0].RECEPTNO_TO.value > 0) && (document.forms[0].RECEPTNO_FROM.value == "")){
			alert("開始番号を入力して下さい。");
			return;
		}
		//ALL'0'の場合に'0'変換(.testで真偽を返す)
	    if (/^0+$/.test(no_from)) {
    	    sns = 0;
	    } else if (/^0+$/.test(no_to)){
    	    ens = 0;
	    } else {
			//ZERO･サプレス
    	    var sn2 = no_from.replace(/^0+/, "");
    	    var en2 = no_to.replace(/^0+/, "");
			//整数変換
        	sns = parseInt(sn2);
        	ens = parseInt(en2);
	    }

		//開始終了印刷範囲のチェック
		if((((no3 < sns) || (no2 > sns)) && ((ens > no3) || (ens < no2)))){
			alert("受付番号が範囲外です(" + no2 + "～" + no3 + ")");
			document.forms[0].RECEPTNO_FROM.value = "";
			document.forms[0].RECEPTNO_TO.value = "";
			return;
		}
		if((no3 < sns) || (no2 > sns)){
			alert("受付番号が範囲外です(" + no2 + "～" + no3 + ")");
			document.forms[0].RECEPTNO_FROM.value = "";
			return;
		}else if((ens > no3) || (ens < no2)){
			alert("受付番号が範囲外です(" + no2 + "～" + no3 + ")");
			document.forms[0].RECEPTNO_TO.value = "";
			return;
		}
	} else {
			document.forms[0].RECEPTNO_FROM.value = "";
			document.forms[0].RECEPTNO_TO.value = "";
	}

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;

    document.forms[0].RECEPTNO_FROM.value = "";
    document.forms[0].RECEPTNO_TO.value = "";
    document.forms[0].LINE.value = "1";
    document.forms[0].ROW.value = "1";

}

//受付番号の大小チェック
function checkedReceptNo() {

	var no_from = document.forms[0].RECEPTNO_FROM.value;    //受付番号(開始)
	var no_to   = document.forms[0].RECEPTNO_TO.value; 	    //受付番号(終了)
	var sns ;
	var ens ;
	var irekae = '';

    //ALL'0'の場合に'0'変換(.testで真偽を返す)
    if (/^0+$/.test(no_from)) {
        sns = 0;
    } else if (/^0+$/.test(no_to)){
        ens = 0;
    } else {
        //ZERO･サプレス
        var sn2 = no_from.replace(/^0+/, "");
        var en2 = no_to.replace(/^0+/, "");
        //整数変換
        sns = parseInt(sn2);
        ens = parseInt(en2);
    }

    if(sns > ens) {
        irekae      = no_to;
        no_to       = no_from;
        no_from     = irekae;
        document.forms[0].RECEPTNO_FROM.value   = no_from;
        document.forms[0].RECEPTNO_TO.value     = no_to;
    }

    return true;
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
    
function AllClearList(OptionList, TitleName) 
{
    attribute = document.forms[0].CATEGORY_NAME;
    ClearList(attribute,attribute);
    attribute = document.forms[0].CATEGORY_SELECTED;
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
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;  
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
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    }
    else
    {  
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;  
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
