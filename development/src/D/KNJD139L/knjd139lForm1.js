//サブミット
function btn_submit(cmd) {

    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }

    //update時の入力チェックは動的にしているので、チェック不要。
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
