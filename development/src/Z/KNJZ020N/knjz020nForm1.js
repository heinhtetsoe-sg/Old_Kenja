function btn_submit(cmd) {

    if (cmd == 'list'){
        parent.right_frame.location.href='knjz020nindex.php?cmd=edit&year='+document.forms[0].year.value+'&APPLICANTDIV='+document.forms[0].APPLICANTDIV.value+'&TESTDIV='+document.forms[0].TESTDIV.value;
    }

    if (cmd == 'copy') {
        var value = eval(document.forms[0].year.value) + 1;
        var message = document.forms[0].year.value + '年度のデータから、' + value + '年度にデータをコピーします。';
        if (!confirm('{rval MSG101}\n\n' + message)) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
