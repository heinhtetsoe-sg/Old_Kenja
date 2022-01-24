function btn_submit(cmd) {
    if (cmd == 'list') {
        var entexamyear = document.forms[0].ENTEXAMYEAR.value;
        var applicantdiv = document.forms[0].APPLICANTDIV.value;
        var qualifiedCd = document.forms[0].QUALIFIED_CD.value;
        parent.right_frame.location.href='knjlz11hindex.php?cmd=edit&chFlg=1&ENTEXAMYEAR=' + entexamyear + '&APPLICANTDIV=' + applicantdiv + '&QUALIFIED_CD=' + qualifiedCd;
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
