function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//印刷
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

    if (document.forms[0].EXAMCNT.value <= 0 || !document.forms[0].EXAMCNT.value){
        alert('人数を指定してください。');
        return false;
    }
    //NO001
    if (document.forms[0].EXAMCNT.value > 50){
        alert('人数がオーバーしています。\r(最大50人)');
        return false;
    }

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

