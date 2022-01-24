function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function kin(obj, idx) {
    var dis = false;
    var i, j;
    for (i = 0; i < idx.length; i++) {
        j = idx[i];
        if (obj[j] && obj[j].selected) {
            dis = true;
        }
    }
    document.forms[0].SCHLTIME.disabled = dis;
}

function newwin(SERVLET_URL){

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
