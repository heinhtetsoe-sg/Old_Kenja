function btn_submit(cmd)
{
    //「取消ボタン」押下
    if (cmd == "reset") {
        if (confirm("{rval MSG106}") == false) {
            return false;
        }
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
