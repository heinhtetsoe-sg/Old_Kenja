//サブミット
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Cleaning(that) {
    var str = 'knjg042index.php?cmd=edit&apply_div=' + that.value;
    btn_submit('list');
    window.open(str,'right_frame');
    return false;
}

function ChangeSelection_perm(that) {
    var str = 'knjg042index.php?cmd=edit&perm_div=' + that.value;
    btn_submit('list');
    window.open(str,'right_frame');
    return false;
}
