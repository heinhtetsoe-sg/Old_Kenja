function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function kin(obj) {

    if (obj[1].selected || obj[2].selected || obj[3].selected){
        document.forms[0].SCHLTIME.disabled = true;
    }else {
        document.forms[0].SCHLTIME.disabled = false;
    }

}

function newwin(SERVLET_URL){

    if (document.forms[0].DATEF.value == ''){
        alert("開始日付を指定して下さい。");
        return false;
    }
    if (document.forms[0].DATET.value == ''){
        alert("終了日付を指定して下さい。");
        return false;
    }

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
