function btn_submit(cmd){

    //学籍番号チェック
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    if (cmd == 'bukatu'){         //部活動参照
        loadwindow('knja120bindex.php?cmd=bukatu',0,0,650,350);
        return true;
    } else if (cmd == 'iinkai'){  //委員会参照
        loadwindow('knja120bindex.php?cmd=iinkai',0,0,700,350);
        return true;
    } else if (cmd == 'tuutihyou'){  //通知票所見参照
        loadwindow('knja120bindex.php?cmd=tuutihyou',0,0,750,450);
        return true;

    } else if (cmd == 'sikaku'){  //資格参照
        loadwindow('knja120bindex.php?cmd=sikaku',0,0,700,350);
        return true;
    } else if (cmd == 'tyousasyo'){  //調査書(進学用)出欠の記録参照
        loadwindow('knja120bindex.php?cmd=tyousasyo',0,0,400,200);
        return true;
    }

    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }

    updBtnNotDisp();

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function updBtnNotDisp() {
    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_up_pre.disabled = true;
    document.forms[0].btn_up_next.disabled = true;
}
