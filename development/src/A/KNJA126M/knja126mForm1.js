function btn_submit(cmd){

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    } else if (cmd == 'subform1'){  //資格参照
        loadwindow('knja126mindex.php?cmd=subform1',0,0,700,500);
        return true;
    } else if (cmd == 'subform2'){  //部活動参照
        loadwindow('knja126mindex.php?cmd=subform2',0,0,700,350);
        return true;
    } else if (cmd == 'subform3'){  //委員会参照
        loadwindow('knja126mindex.php?cmd=subform3',0,0,700,350);
        return true;
    } else if (cmd == 'subform4'){  //調査書（進学用）の出欠の記録参照
        loadwindow('knja126mindex.php?cmd=subform4',0,0,360,180);
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
