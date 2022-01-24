function btn_submit(cmd) {
    if (cmd == "list") {
        
        document.forms[0].hope.value = "edit";    
    } 
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closing_window(cd){

    if(cd){
        alert('{rval MSG305} \r\n対象年度に転入生が存在しません。');
    }else{
        alert('{rval MSG300}');
    }
        closeWin();
        return true;
}
