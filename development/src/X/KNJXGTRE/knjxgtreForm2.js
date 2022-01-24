function opener_submit(){

    var APPDATE = CHAIRCD = sep = "";
    for (var i=0; i < document.forms[0].elements.length; i++)
    {
        if (document.forms[0].elements[i].name == "CHAIRCD[]" && document.forms[0].elements[i].checked){
            var tmp = document.forms[0].elements[i].value;
            var arr = tmp.split(',');
            CHAIRCD += sep + arr[0];
            APPDATE += sep + arr[0] + '-' + arr[1];
            sep = ",";
        }
    }
    if (CHAIRCD != ''){
        top.opener.document.forms[0].CHAIRCD.value = CHAIRCD;
        top.opener.document.forms[0].APPDATE.value = APPDATE;
        top.opener.document.forms[0].cmd.value = "left";
        top.opener.document.forms[0].submit();
        top.window.close();
        return false;
    }else{
        alert("チェックボックスが選択されていません。");
        return true;
    }
}
function check_all(obj){
    for (var i=0; i < document.forms[0].elements.length; i++)
    {
        if (document.forms[0].elements[i].name == "CHAIRCD[]"){
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}
