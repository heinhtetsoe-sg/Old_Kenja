function opener_submit(){
    var subclass;
    var attend;
    var groupcd;
    var staffcd;	//2004-07-30 naka
    var sep;
    
    staffcd = subclass = attend = groupcd = sep = "";
    for (var i=0;i<document.forms[0].elements.length;i++)
    {
        var e = document.forms[0].elements[i];
        if (e.type=='checkbox' && e.checked && e.name!='chk_all'){
            var val = e.value;
            if (val != ''){
                var tmp = val.split(',');
            
                subclass += sep+tmp[0];
                attend += sep+tmp[1];
                groupcd += sep+tmp[2];
                staffcd += sep+tmp[3];		//2004-07-30 naka
                sep = ",";
            }
        }
    }
	var i = window.parent.left_frame.document.forms[0].SEL_SEMI.selectedIndex;
    var tmp = window.parent.left_frame.document.forms[0].SEL_SEMI.options[i].value.split(',')
    if (subclass != '' && attend != '' && groupcd != ''){
        top.opener.document.forms[0].YEAR.value = tmp[0];
        top.opener.document.forms[0].SEMESTER.value = tmp[1];
/* 04/10/30 カット
        top.opener.document.forms[0].TESTKINDCD.value = document.forms[0].TESTKINDCD.value;
        top.opener.document.forms[0].TESTITEMCD.value = document.forms[0].TESTITEMCD.value;
*/
        top.opener.document.forms[0].CLASSCD.value = subclass.substr(0,2);
        top.opener.document.forms[0].SUBCLASSCD.value = subclass;
        top.opener.document.forms[0].ATTENDCLASSCD.value = attend;
        top.opener.document.forms[0].STAFF_CD.value = staffcd;			//2004-07-30 naka
        top.opener.document.forms[0].GROUPCD.value = groupcd;
        top.opener.document.forms[0].DISP.value = document.forms[0].DISP.value;
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

