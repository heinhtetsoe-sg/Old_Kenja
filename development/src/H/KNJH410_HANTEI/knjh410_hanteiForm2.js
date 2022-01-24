function btn_submit(cmd) {   
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function closecheck() {
    parent.window.close();
}
function openGamen(gyousya){
    
    window.open('knjh410_hanteiindex.php?cmd=reappear&GYOUSYA='+gyousya,'bottom_frame');    
    
}
