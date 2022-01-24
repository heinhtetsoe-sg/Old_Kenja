function btn_submit(cmd) {

    //チェック
    if ((cmd == 'update') || (cmd == 'insert')){
        if (document.forms[0].SCHREGNO.value == ""){
            alert('{rval MSG304}');
            return true;
        } else if ((document.forms[0].VISIT_DATE.value == "") || (document.forms[0].VISIT_HOUR.value == "") || (document.forms[0].VISIT_MINUTE.value == "")){
            alert('来室日時が入力されていません。\n　　　　（必須入力）');
            return true;
        }
    } else if (cmd == 'subform4_clear'){
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

    alert('登録された内容が印刷されます。');

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
