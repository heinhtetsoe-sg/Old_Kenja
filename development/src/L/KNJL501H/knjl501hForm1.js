function btn_submit(cmd) {
    if (cmd == 'list'){
        parent.right_frame.location.href='knjl501hindex.php?cmd=edit&year='+document.forms[0].year.value+'&APPLICANTDIV='+document.forms[0].APPLICANTDIV.value;
    }

    if (cmd == 'copy') {
        var value = eval(document.forms[0].year.value) + 1;
        var message = document.forms[0].year.value + '年度のデータから、' + value + '年度にデータをコピーします。';
        if (!confirm('{rval MSG101}\n\n' + message)) {
            return false;
        }
    }

    //読込中は、次年度作成ボタンをグレーアウト
    document.forms[0].btn_year_add.disabled      = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
