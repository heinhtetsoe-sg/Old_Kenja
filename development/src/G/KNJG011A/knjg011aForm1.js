function btn_submit(cmd) {
        if (cmd == 'execute') {
            if (document.forms[0].SCH_CNT_CHK.value < 1) {
                alert('発行対象人数が０名です。');
                return;
            }
            if (document.forms[0].GRADE.value == "") {
                alert('学年を指定してください。');
                return;
            }
            if (document.forms[0].CERTIF_KINDCD.value == "") {
                alert('証明書種類を指定してください。');
                return;
            }
            if (document.forms[0].CERTIF_NO_MAX.value == "") {
                alert('発行番号を入力してください。');
                return;
            }
            if (parseInt(document.forms[0].CERTIF_NO_MAX.value) <= parseInt(document.forms[0].CERTIF_NO_CHK.value)) {
                alert('最終発行番号より大きい番号を入力してください。');
                return;
            }
            if (!confirm("処理を実行します。よろしいですか？")) {
                return;
            }
        }
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
