function btn_submit(cmd) {
    if (cmd == 'csv') {
        if (!document.forms[0].JUDGEMENT1.checked && !document.forms[0].JUDGEMENT2.checked && !document.forms[0].JUDGEMENT3.checked) {
            alert('どちらかを選択してください');
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_check(obj) {
    obj_name = obj.name;
    if (obj_name == 'JUDGEMENT1' || obj_name == 'JUDGEMENT2') {
        if (obj.checked) {
            document.forms[0].JUDGEMENT3.checked = false;
        }
    } else {
        if (obj.checked) {
            document.forms[0].JUDGEMENT1.checked = false;
            document.forms[0].JUDGEMENT2.checked = false;
        }
    }
}

function newwin(SERVLET_URL) {
    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
