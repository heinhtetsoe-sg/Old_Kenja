function btn_submit(cmd) {
    if (cmd == 'list'){
        parent.right_frame.location.href='knjl413hindex.php?cmd=edit&chFlg=1&year='+document.forms[0].year.value+'&APPLICANTDIV='+document.forms[0].APPLICANTDIV.value+'&EXAMCOURSECD='+document.forms[0].EXAMCOURSECD.value;
    }

    //次年度コピー
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
