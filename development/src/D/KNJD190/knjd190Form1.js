function opener_submit(SERVLET_URL){

    var subclass;
    var attend;
    var groupcd;
    var sep;
    var j;
    
    subclass = attend = groupcd = sep = "";
    for (var i=0,j=0;i<document.forms[0].elements.length;i++)
    {
        var e = document.forms[0].elements[i];
        if (e.type=='checkbox' && e.checked && e.name!='chk_all'){
            var val = e.value;
            if (val != ''){
                var tmp = val.split(',');
            
                subclass += sep+tmp[0];
                attend += sep+tmp[1];
                groupcd += sep+tmp[2];
                sep = ",";
            }
        }
    }
    if (attend != '' && groupcd != ''){
        document.forms[0].ATTENDCLASSCD.value = attend;
        document.forms[0].GROUPCD.value = groupcd;

        action = document.forms[0].action;
        target = document.forms[0].target;

	    document.forms[0].action = SERVLET_URL +"/KNJD";
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
