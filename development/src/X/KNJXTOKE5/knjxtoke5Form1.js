function opener_submit(semester){
    var appdate;	//2004-08-11 naka
    var staffcd;	//2004-08-11 naka
    var subclass;
    var attend;
    var groupcd;
    var sep;
    
    appdate = staffcd = subclass = attend = groupcd = sep = "";
    for (var i=0;i<document.forms[0].elements.length;i++)
    {
        var e = document.forms[0].elements[i];
        if (e.type=='radio' && e.checked && e.name!='chk_all'){
            var val = e.value;
            if (val != ''){
                var tmp = val.split(',');
            
                subclass += sep+tmp[0];
                attend += sep+tmp[1];
                groupcd += sep+tmp[2];
                staffcd += sep+tmp[3];	//2004-08-11 naka
                appdate += sep+tmp[4];	//2004-08-11 naka
                sep = ",";
            }
        }
    }
    if (subclass != '' && attend != '' && groupcd != ''){
        top.opener.document.forms[0].CLASSCD.value = subclass.substr(0,2);
        top.opener.document.forms[0].SUBCLASSCD.value = subclass;
        top.opener.document.forms[0].ATTENDCLASSCD.value = attend;
        top.opener.document.forms[0].GROUPCD.value = groupcd;
        top.opener.document.forms[0].NAME_SHOW.value = staffcd;	//2004-08-11 naka
        top.opener.document.forms[0].APPDATE.value = appdate;	//2004-08-11 naka
        top.opener.document.forms[0].SEMESTER.value = semester;
        top.opener.document.forms[0].cmd.value = 'toukei';
        top.opener.document.forms[0].submit();
        top.window.close();
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
        if (e.type=='checkbox' && e.name != "chk_all"){
            e.checked = flg;
        }
    }
}

