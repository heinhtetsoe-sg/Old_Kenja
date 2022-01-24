function btn_submit(cmd) {
    if (cmd === 'update') {
        if (document.forms[0].KOUNYU_L_M_S_CD.value == "") {
            alert('{rval MSG301}' + '(品名等)');
            return false;
        }
    } else if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return false;
    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

    if (document.forms[0].category_selected.length == 0)
    {
        alert('{rval MSG916}');
        return;
    }

    for (var i = 0; i < document.forms[0].category_name.length; i++)
    {  
        document.forms[0].category_name.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].category_selected.length; i++)
    {  
        document.forms[0].category_selected.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJM";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}