function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//印刷
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function checkOutput(obj) {
    if (obj.value == '2') {
        document.forms[0].OUTPUT4.disabled = true;
        document.forms[0].OUTPUT4.checked  = false;
        document.forms[0].ROW1.disabled = true;
        document.forms[0].ROW2.disabled = true;
    } else {
        document.forms[0].OUTPUT4.disabled = false;
        document.forms[0].ROW1.disabled = false;
        document.forms[0].ROW2.disabled = false;
    }
}

//旧クラスを選択したとき
function checkOutput2(obj) {
    if (obj.checked) {
        document.forms[0].ROW1.disabled = true;
        document.forms[0].ROW2.disabled = true;
    }else{
        document.forms[0].ROW1.disabled = false;
        document.forms[0].ROW2.disabled = false;
    }
}
