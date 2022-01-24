function btn_submit(cmd) {
    if (cmd == 'list') {
        var divVal;
        divVal = (document.getElementById("GROUPDIV1").checked == true) ? "1": "2";
        parent.right_frame.location.href='knjl066iindex.php?cmd=edit&chFlg=1&ENTEXAMYEAR=' + document.forms[0].ENTEXAMYEAR.value + '&GROUPDIV=' + divVal;
    }

    //次年度コピー
    if (cmd == 'copy') {
        var value = eval(document.forms[0].ENTEXAMYEAR.value) + 1;
        var message = document.forms[0].ENTEXAMYEAR.value + '年度のデータから、' + value + '年度にデータをコピーします。';
        if (!confirm('{rval MSG101}\n\n' + message)) {
            return false;
        }
    }

    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
