function btn_submit(cmd) {
    if (cmd == 'list'){
        parent.right_frame.location.href='knjl515hindex.php?cmd=edit&YEAR='+document.forms[0].YEAR.value+'&APPLICANTDIV='+document.forms[0].APPLICANTDIV.value;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
