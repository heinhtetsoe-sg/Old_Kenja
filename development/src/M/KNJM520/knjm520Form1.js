//  漢字



window.onload = function (){
    document.forms[0].DATE.disabled = true;
    document.forms[0].btn_calen.disabled = true;
}

function useableTextBox() {
    if (document.forms[0].PRINT_DIV[1].checked) {
        document.forms[0].DATE.disabled = false;
        document.forms[0].btn_calen.disabled = false;
    } else {
        document.forms[0].DATE.disabled = true;
        document.forms[0].btn_calen.disabled = true;
    }
}

function btn_submit(cmd){
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){
    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url + "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJM";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}



