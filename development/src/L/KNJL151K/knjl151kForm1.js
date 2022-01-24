function btn_submit(cmd) {
    if (cmd == 'execute') {
        if (!confirm('{rval MSG101}')) {
            return false;
        }
        document.forms[0].btn_output.disabled = true;
        document.forms[0].btn_ok.disabled = true;
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

function OutputFile(filename)
{
    parent.top_frame.location.href=filename;
}
