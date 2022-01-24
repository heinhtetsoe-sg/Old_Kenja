// kanji=漢字

function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){

    var proArray = document.forms[0].useRadioPattern.value.split("-"); 
    for (var i = 0; i < proArray.length; i++) {
        if (document.forms[0].GROUP_DIV[i].checked) {
           document.forms[0].FORM_GROUP_DIV.value = proArray[i];
        }
    }

    if (!document.forms[0].PROFICIENCYDIV.value || !document.forms[0].PROFICIENCYCD.value || !document.forms[0].GRADE.value) {
        alert('{rval MSG916}');
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;
//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";

    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();
    
    document.forms[0].action = action;
    document.forms[0].target = target;
}
