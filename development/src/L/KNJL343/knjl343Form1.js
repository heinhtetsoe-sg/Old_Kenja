function btn_submit(cmd) {
    if (cmd == 'printCheck') {
        if (document.forms[0].NOTICEDAY.value == ""){
            alert("通知日付を入力して下さい");
            return;
        }
        if ((document.forms[0].OUT.value == 2)&&(document.forms[0].EXAMNO.value == "")){
            alert("受験番号を入力して下さい");
            return;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){
    action = document.forms[0].action;
    target = document.forms[0].target;

	url = location.hostname;
//	document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

