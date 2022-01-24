function btn_submit(cmd) {
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//画面の切替
function Page_jumper(link, auth) {
    
    link = link + "/D/KNJD129F/knjd129findex.php?CALL_PRGID=KNJD129J&SEND_AUTH=" + auth;
    parent.location.href=link;
}
