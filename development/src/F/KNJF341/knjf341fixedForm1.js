function btn_submit(cmd)
{
    if (cmd == 'fixedUpd') {
        if (document.forms[0].FIXED_DATE.value == "") {
            alert('確定日を入力して下さい');
            return true;
        }

        parent.document.forms[0].FIXED_DATE.value = document.forms[0].FIXED_DATE.value;
        parent.document.forms[0].cmd.value = cmd;
        parent.document.forms[0].submit();
    }

    //確定画面を閉じる
    parent.closeit();
}
