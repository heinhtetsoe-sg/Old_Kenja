function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){

    if(document.forms[0].TITLE.length == 0 || document.forms[0].FACCD_NAME1.length == 0 || document.forms[0].FACCD_NAME2.length == 0)
    {
        alert("指定範囲が正しく有りません。");
        return;
    }

    var check_from   = document.forms[0].FACCD_NAME1;
    var check_to     = document.forms[0].FACCD_NAME2;
    var irekae   = "";
    
    if(check_from.value > check_to.value) {
        irekae           = check_to.value;
        check_to.value   = check_from.value;
        check_from.value = irekae;
    }

    var d = document.forms[0].TITLE.value;
    var tmp = d.split(',');
    document.forms[0].YEAR.value = tmp[0];
    document.forms[0].BSCSEQ.value = tmp[1];
    document.forms[0].SEMESTER.value = tmp[2];

    action = document.forms[0].action;
    target = document.forms[0].target;

//    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJB";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
