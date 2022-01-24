function btn_submit(cmd) {
    if (cmd == 'list') {
        var leftYear = document.forms[0].ENTEXAMYEAR.value;
        
        parent.right_frame.location.href='knjl503iindex.php?cmd=edit&chFlg=1&ENTEXAMYEAR=' + leftYear;
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

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
