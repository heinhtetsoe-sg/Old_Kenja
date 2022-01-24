function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){
    if (document.forms[0].OUTPUT[0].checked)
    {
        if ((document.forms[0].E_SCHREGNO.value.length == 0 || document.forms[0].S_SCHREGNO.value.length == 0))
        {
            alert('学籍番号のFROM, TOを入力して下さい');
            return;
        }
        if (document.forms[0].E_SCHREGNO.value < document.forms[0].S_SCHREGNO.value)
        {
            var tmp = document.forms[0].E_SCHREGNO.value;
            document.forms[0].E_SCHREGNO.value = document.forms[0].S_SCHREGNO.value;
            document.forms[0].S_SCHREGNO.value = tmp;
        }
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

function check(obj){
    
    if (!obj.value || obj.value == 0){
        alert('出力部数を指定して下さい。');
        obj.value = '1';
        obj.focus();
    }
}

function setFormOnly() {
    if (document.forms[0].FORM_ONLY.checked) {
        document.forms[0].OUTPUT[0].disabled = true;
        document.forms[0].OUTPUT[1].disabled = true;
        document.forms[0].S_SCHREGNO.disabled = true;
        document.forms[0].E_SCHREGNO.disabled = true;
    } else {
        document.forms[0].OUTPUT[0].disabled = false;
        document.forms[0].OUTPUT[1].disabled = false;
        document.forms[0].S_SCHREGNO.disabled = false;
        document.forms[0].E_SCHREGNO.disabled = false;
    }
}
