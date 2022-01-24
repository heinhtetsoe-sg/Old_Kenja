//サブミット
function btn_submit(cmd) {
    if (cmd == 'clear'){
        if (confirm("{rval MSG106}")){
            cmd = "form2";
        }else{
            return true;
        }    
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
