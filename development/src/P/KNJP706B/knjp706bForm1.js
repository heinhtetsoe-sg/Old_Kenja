function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//情報画面表示
function dispInfo(form){
    document.getElementById("btn_end").style.display = "";
    document.getElementById("btn_rtrn").style.display = "";	//NO002
    parent.bottom_frame.location.replace('knjp706bindex.php?cmd='+form);
}


