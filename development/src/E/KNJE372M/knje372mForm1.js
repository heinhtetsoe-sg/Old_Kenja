function btn_submit(cmd) {

    if (cmd == 'execute') {
        if (document.forms[0].STAFF_AUTH.value != document.forms[0].PASS_AUTH.value) {
            alert('{rval MSG300}\n更新可のみ処理が可能です。');
            return false;
        }

        if (document.forms[0].TOROKU_DATE.value == '') {
            alert("登録日を入力してください。");
            return false;
        }
        if (confirm('{rval MSG101}')) {
            document.all('marq_msg').style.color = '#FF0000';
        } else {
            return;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}\n更新可のみ処理が可能です。');
    closeWin();
}

function OutputFile(filename) {
    parent.top_frame.location.href=filename;
}
