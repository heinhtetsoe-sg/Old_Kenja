function btn_submit(cmd)
{
    if (cmd == 'reset' && !confirm('{rval MSG106}')){
        return;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function setChangeColor(taisyou) {
    document.getElementById(taisyou).bgColor = "#ccffcc";
}
