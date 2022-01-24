    function btn_submit(cmd){

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    } else if (cmd == 'form2'){
        loadwindow('knjd139pindex.php?cmd=form2',0,0,600,500);
        return true;
    } else if (cmd == 'subform1'){
        loadwindow('knjd139pindex.php?cmd=subform1',0,0,700,300);
        return true;
    } else if (cmd == 'subform2'){
        loadwindow('knjd139pindex.php?cmd=subform2',0,0,700,300);
        return true;
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//Submitしない
function btn_keypress(){
    if (event.keyCode == 13){
        event.keyCode = 0;
        window.returnValue  = false;
    }
}
