function btn_submit(cmd) {
    //実行
    if (cmd == "exec") {
        document.forms[0].encoding = "multipart/form-data";

        if (!confirm('{rval MSG101}')) {
            return false;
        }

        if (document.forms[0].OUTPUT[0].checked) {
            cmd = "csvOutput";
        } else if (document.forms[0].OUTPUT[1].checked) {
            cmd = "csvInput";
        } else {
            cmd = "errOutput";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
