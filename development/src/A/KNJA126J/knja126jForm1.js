function btn_submit(cmd){

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    } else if (cmd == 'form2'){         //行動の記録・特別活動の記録
        loadwindow('knja126jindex.php?cmd=form2',0,0,750,650);
        return true;
    } else if (cmd == 'subform1'){      //通知表所見参照
        if(document.forms[0].SEMES_CNT.value == 3) {
            loadwindow('knja126jindex.php?cmd=subform1',0,100,750,400);
        } else {
            loadwindow('knja126jindex.php?cmd=subform1',0,150,750,320);
        }
        return true;
    } else if (cmd == 'subform2'){      //出欠の記録参照
        loadwindow('knja126jindex.php?cmd=subform2',0,150,750,280);
        return true;
    } else if (cmd == 'shokenlist1'){  //既入力内容を参照（総合的な学習時間）
        loadwindow('knja126jindex.php?cmd=shokenlist1',0,0,700,350);
        return true;
    } else if (cmd == 'shokenlist2'){  //既入力内容を参照（総合所見）
        loadwindow('knja126jindex.php?cmd=shokenlist2',0,0,700,350);
        return true;
    } else if (cmd == 'shokenlist3'){  //既入力内容を参照（出欠の記録備考）
        loadwindow('knja126jindex.php?cmd=shokenlist3',0,0,700,350);
        return true;
    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
