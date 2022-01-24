function btn_submit(cmd) {

    if (cmd == 'regd' || 'score' || 'attend' || 'record' || 'grad' || 'user') {
        document.getElementById('marq_msg').innerHTML = '更新中です...しばらくおまちください';
        document.getElementById('marq_msg').style.color = '#FF0000';
        //読み込み中は、ボタンをグレー（押せないよう）にする。
        document.forms[0].btn_regd.disabled = true;
        document.forms[0].btn_score1.disabled = true;
        document.forms[0].btn_score2.disabled = true;
        document.forms[0].btn_score4.disabled = true;
        document.forms[0].btn_score5.disabled = true;
        document.forms[0].btn_attend.disabled = true;
        document.forms[0].btn_record.disabled = true;
        document.forms[0].btn_grad.disabled = true;
        document.forms[0].btn_user.disabled = true;
        document.forms[0].btn_end.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
