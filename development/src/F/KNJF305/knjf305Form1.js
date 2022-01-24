function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    if (cmd == 'update' || cmd == 'houkoku') {
        //更新時
        if (cmd == 'update') {
            if (document.forms[0].ACTION_S_DATE.value > document.forms[0].ACTION_E_DATE.value) {
                alert('{rval MSG916}'+'(措置期間)');
                return false;
            }
        }
        //報告時
        if (cmd == 'houkoku') {
            if (document.forms[0].EXECUTE_DATE.value == "") {
                alert('{rval MSG304}'+'(作成日)');
                return false;
            }
        }
        
        if (document.forms[0].DISEASECD.value == "") {
            alert('{rval MSG304}'+'(理由（疾患名）)');
            return false;
        }
        
        if (document.forms[0].HEISA_DIV_VALUE.value == "1") {
            if (document.forms[0].GRADE.value == "") {
                alert('{rval MSG304}'+'(学年)');
                return false;
            }
            if (document.forms[0].HR_CLASS.value == "") {
                alert('{rval MSG304}'+'(組)');
                return false;
            }
        } else if (document.forms[0].HEISA_DIV_VALUE.value == "2") {
            if (document.forms[0].GRADE.value == "") {
                alert('{rval MSG304}'+'(学年)');
                return false;
            }
        }
        if (document.forms[0].ACTION_S_DATE.value == "") {
            alert('{rval MSG304}'+'(措置開始日)');
            return false;
        }
        //報告時
        if (cmd == 'houkoku') {
            if (!confirm('{rval MSG108}')) return false;
        }
    }
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){
    if (document.forms[0].EXECUTE_DATE.value == "") {
        alert('{rval MSG304}'+'(作成日)');
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}


