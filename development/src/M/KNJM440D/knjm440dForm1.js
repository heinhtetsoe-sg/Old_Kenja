function btn_submit(cmd) {

    if (cmd == 'update' && document.forms[0].DATA_CNT.value > 0) {
        if (!confirm('既に受験資格判定結果が登録されています。\n削除して再判定されます。よろしいですか。'))
            return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
