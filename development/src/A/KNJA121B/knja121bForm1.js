function btn_submit(cmd){

    //学籍番号チェック
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    if (cmd == 'tuutihyou'){        //通知票所見参照
        loadwindow('knja121bindex.php?cmd=tuutihyou',0,0,750,450);
        return true;
    } else if (cmd == 'koudou'){    //行動の記録備考
        loadwindow('knja121bindex.php?cmd=koudou',0,0,400,580);
        return true;
    }

    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
