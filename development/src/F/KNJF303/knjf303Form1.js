function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }
    
    //必須項目のNULLチェック
    if (cmd == 'update' || cmd == 'houkoku') {
        if (cmd == 'houkoku') {
            if (document.forms[0].EXECUTE_DATE.value == "") {
                alert('{rval MSG304}'+'(作成日)');
                return false;
            }
        }
        if (document.forms[0].DATA_DIV.value == "") {
            if (cmd == 'update') {
                alert('{rval MSG301}'+'新規/作成済みデータかが選択されていません。');
            } else {
                alert('{rval MSG301}'+'報告するデータが選択されていません。');
            }
            return false;
        }
        if (document.forms[0].SUSPEND_DIRECT_DATE.value == "") {
            alert('{rval MSG301}'+'(出席停止を指示した日)');
            return false;
        }
        if (document.forms[0].DISEASECD.value == "") {
            alert('{rval MSG301}'+'(理由（疾患病）)');
            return false;
        }
        if (document.forms[0].SUSPEND_DIRECT_DATE.value != "") {
            if (document.forms[0].SEM_SDATE.value > document.forms[0].SUSPEND_DIRECT_DATE.value || document.forms[0].SEM_EDATE.value < document.forms[0].SUSPEND_DIRECT_DATE.value) {
                alert('{rval MSG916}'+'\n出席停止を指示した日は' + document.forms[0].SEM_SDATE.value + '～' + document.forms[0].SEM_EDATE.value + 'の範囲で指定して下さい。');
                return false;
            }
        }
    }
    
    //更新時
    if (cmd == 'update' && document.forms[0].SUSPEND_E_DATE.value != "") {
        if (document.forms[0].SUSPEND_DIRECT_DATE.value > document.forms[0].SUSPEND_E_DATE.value) {
            alert('{rval MSG916}'+'(出席停止期間)');
            return false;
        }
    }
    
    //報告時
    if (cmd == 'houkoku') {
        if (document.forms[0].DATA_DIV.value == '0_new') {
            alert('{rval MSG304}'+'\n報告するデータは作成済みのデータを選択して下さい。');
            return false;
        }
        if (!confirm('{rval MSG108}')) return false;
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

