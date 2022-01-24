function btn_submit(cmd){

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    } else if (cmd == 'form2'){         //行動の記録・特別活動の記録
        loadwindow('knja126pindex.php?cmd=form2',0,0,600,650);
        return true;
    } else if (cmd == 'subform1'){      //通知表所見参照
        if(document.forms[0].SEMES_CNT.value == 3) {
            loadwindow('knja126pindex.php?cmd=subform1',0,200,750,400);
        } else {
            loadwindow('knja126pindex.php?cmd=subform1',0,250,750,320);
        }
        return true;
    } else if (cmd == 'subform2'){      //出欠の記録参照
        loadwindow('knja126pindex.php?cmd=subform2',0,200,750,280);
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
