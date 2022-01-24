function opener_submit(SERVLET_URL){
    document.forms[0].PRGID.value = "KNJA233";

    var appdate;
    var chargediv;
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
            
                appdate += sep+tmp[5];
                chargediv += sep+tmp[4];
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
        document.forms[0].CHARGEDIV.value = chargediv;
        document.forms[0].APPDATE.value = appdate;

        if (document.forms[0].KENSUU.value < 1)
        {
            alert("出力件数を指定して下さい。");
            return;
        }

        action = document.forms[0].action;
        target = document.forms[0].target;

//        url = location.hostname;
//        document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
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

function csv_submit(cmd){
    document.forms[0].PRGID.value = "KNJA233";

    var appdate;
    var chargediv;
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
            
                appdate += sep+tmp[5];
                chargediv += sep+tmp[4];
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
        document.forms[0].CHARGEDIV.value = chargediv;
        document.forms[0].APPDATE.value = appdate;

        if (document.forms[0].KENSUU.value < 1)
        {
            alert("出力件数を指定して下さい。");
            return;
        }

        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }else{
        alert("チェックボックスが選択されておりません。");
        return true;
    }
}

function hurigana() {
    if (document.forms[0].OUTPUT[2].checked) {
        document.forms[0].HURIGANA_OUTPUT[0].disabled = true;
        document.forms[0].HURIGANA_OUTPUT[1].disabled = true;
    } else {
        document.forms[0].HURIGANA_OUTPUT[0].disabled = false;
        document.forms[0].HURIGANA_OUTPUT[1].disabled = false;
    }
}

//エクセル出力
function newwin(SERVLET_URL, schoolCd, fileDiv) {
    document.forms[0].encoding = "application/x-www-form-urlencoded";
    document.forms[0].PRGID.value = "KNJA233_XLS";

    var appdate;
    var chargediv;
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
            
                appdate += sep+tmp[5];
                chargediv += sep+tmp[4];
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
        document.forms[0].CHARGEDIV.value = chargediv;
        document.forms[0].APPDATE.value = appdate;

        if (document.forms[0].KENSUU.value < 1)
        {
            alert("出力件数を指定して下さい。");
            return;
        }

        //テンプレート格納場所
        urlVal = document.URL;
        urlVal = urlVal.replace("http://", "");
        var resArray = urlVal.split("/");
        var fieldArray = fileDiv.split(":");
        urlVal = "/usr/local/" + resArray[1] + "/src/etc_system/XLS_TEMP_" + schoolCd + "/CSV_Template" + fieldArray[0] + "." + fieldArray[1];
        document.forms[0].TEMPLATE_PATH.value = urlVal;

        action = document.forms[0].action;
        target = document.forms[0].target;

//        url = location.hostname;
//        document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
        document.forms[0].action = SERVLET_URL +"/KNJA";
        document.forms[0].target = "_blank";
        document.forms[0].submit();

        document.forms[0].action = action;
        document.forms[0].target = target;
    } else {
        alert("チェックボックスが選択されておりません。");
        return;
    }
}
