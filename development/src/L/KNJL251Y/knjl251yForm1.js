function btn_submit(cmd) {
    if(cmd == 'read' || cmd == 'next' || cmd == 'back' || cmd == 'update' || cmd == 'reset') {
        if (document.forms[0].S_EXAMNO.value == '' || eval(document.forms[0].S_EXAMNO.value) == 0) {
            alert('{rval MSG901}' + '\n 受験番号には 1 以上を入力してください');
            return false;
        }
    }    
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    if (cmd == "update" && document.forms[0].HID_RECEPTNO.value == '')  {
        return false;
    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {

    if (document.forms[0].APPLICANTDIV.value == ""){
        alert("入試制度を指定して下さい");
        return;
    }
    if (document.forms[0].TESTDIV.value == ""){
        alert("入試区分を指定して下さい");
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;

}

function showConfirm() {
    if(confirm('{rval MSG106}')) return true;
    return false;
}

function Setflg(obj) {
    change_flg = true;
    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled = true;

    document.getElementById('ROWID' + obj.id).style.background="yellow";
    obj.style.background="yellow";
}