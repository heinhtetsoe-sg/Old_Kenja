function btn_submit(cmd) {

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJP";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function OnNotUse(term)
{
    for (var i=0; i<document.forms[0].elements.length;i++){

        var e = document.forms[0].elements[i];

        if (e.type=='checkbox') {
            if (e.name!='caution_check'+term && e.name!='admonition_check'+term) {
                e.disabled = true;
            } 
        }
    }    
}