function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//î•ñ‰æ–Ê•\¦
function dispInfo(form){
    document.getElementById("btn_end").style.display = "";
    document.getElementById("btn_rtrn").style.display = "";	//NO002
    parent.bottom_frame.location.replace('knjp120kindex.php?cmd='+form);
}


