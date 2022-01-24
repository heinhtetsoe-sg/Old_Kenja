function opener_submit(SERVLET_URL){
    var appdate;//2004/08/07 nakamoto
    var chargediv;//2004/07/27 nakamoto
    var subclass;
    var attend;
    var groupcd;
    var staffcd;
    var sep;
    var j;
    
    appdate = chargediv = subclass = attend = groupcd = staffcd = sep = "";
    for (var i=0,j=0;i<document.forms[0].elements.length;i++)
    {
        var e = document.forms[0].elements[i];
        if (e.type=='checkbox' && e.checked && e.name!='chk_all'){
            var val = e.value;
            if (val != ''){
                var tmp = val.split(',');
            
                appdate += sep+tmp[5];//2004/08/07 nakamoto
                chargediv += sep+tmp[4];//2004/07/27 nakamoto
                subclass += sep+tmp[0];
                attend += sep+tmp[1];
                groupcd += sep+tmp[2];
                staffcd += sep+tmp[3];
                sep = ",";
            }
        }
    }
    if (attend != '' && groupcd != ''){
        document.forms[0].ATTENDCLASSCD.value = attend;
        document.forms[0].GROUPCD.value = groupcd;
        document.forms[0].NAME_SHOW.value = staffcd;
        document.forms[0].CHARGEDIV.value = chargediv;//2004/07/27 nakamoto
        document.forms[0].APPDATE.value = appdate;//2004/08/07 nakamoto

		if (document.forms[0].KENSUU.value < 1)
		{
			alert("出力件数を指定して下さい。");
			return;
		}

        action = document.forms[0].action;
        target = document.forms[0].target;

	    document.forms[0].action = SERVLET_URL +"/KNJA";
    	document.forms[0].target = "_blank";
        document.forms[0].submit();

        document.forms[0].action = action;
        document.forms[0].target = target;

        return false;
    }else{
        alert("チェックボックスが選択されておりません。");
        return true;
    }
    
}
function check_all(){
    var flg;
    
    for (var i=0;i<document.forms[0].elements.length;i++)
    {
        var e = document.forms[0].elements[i];
        if (e.name == "chk_all"){
            flg = e.checked;
        }
        if (e.type=='checkbox' && e.name != "chk_all" && e.name != "chk1" ){
            e.checked = flg;
        }
    }
}
