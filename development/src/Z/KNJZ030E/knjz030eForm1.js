function btn_submit(cmd) {

    if (cmd == 'list'){
        parent.right_frame.location.href='knjz030eindex.php?cmd=edit&ENTEXAMYEAR='+document.forms[0].ENTEXAMYEAR.value+'&APPLICANTDIV='+document.forms[0].APPLICANTDIV.value;
    }

    if (cmd == 'copy') {
        var value = eval(document.forms[0].ENTEXAMYEAR.value) + 1;
        var message = document.forms[0].ENTEXAMYEAR.value + '年度のデータから、' + value + '年度にデータをコピーします。';
        if (!confirm('{rval MSG101}\n\n' + message)) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
