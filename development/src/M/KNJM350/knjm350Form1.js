/*
 * kanji=Š¿Žš
 * <?php # $Id: knjm350Form1.js 56590 2017-10-22 13:01:54Z maeshiro $ ?>
 */
function btn_submit(cmd)
{
    var str = new Object();
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJM";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
