function btn_submit(cmd) {
    if(cmd == 'copy') {
        if (!confirm('{rval MSG101}')) {
            alert('{rval MSG203}');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function rightReload(reductionTarget) {
    window.open("knjp710index.php?cmd=edit&REDUCTION_TARGET=" + reductionTarget, "right_frame");
}