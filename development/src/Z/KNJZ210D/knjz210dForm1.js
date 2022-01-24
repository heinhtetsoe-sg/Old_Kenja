function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    if (cmd == 'update') {
        if (document.forms[0].GRADE.value == "") {
        	alert('学年を選択して下さい。');
            return false;
        }
        if (document.forms[0].SUBCLASSCD.value == "") {
        	alert('科目を選択して下さい。');
            return false;
        }
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(KNJZ210E){

    action = document.forms[0].action;
    target = document.forms[0].target;

    //document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].action = "/Z/KNJZ210E/index.php";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

