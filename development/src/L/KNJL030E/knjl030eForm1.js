function btn_submit(cmd) {
    if (cmd == 'list') {
        var examhall_type = '1';
        if (document.forms[0].EXAMHALL_TYPE[0].checked == true)  examhall_type = document.forms[0].EXAMHALL_TYPE[0].value;
        if (document.forms[0].EXAMHALL_TYPE[1].checked == true)  examhall_type = document.forms[0].EXAMHALL_TYPE[1].value;

        parent.right_frame.location.href='knjl030eindex.php?cmd=edit&ENTEXAMYEAR='+document.forms[0].ENTEXAMYEAR.value+'&APPLICANTDIV='+document.forms[0].APPLICANTDIV.value+'&TESTDIV='+document.forms[0].TESTDIV.value+'&EXAMHALL_TYPE='+examhall_type;
    }

    //次年度コピー
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