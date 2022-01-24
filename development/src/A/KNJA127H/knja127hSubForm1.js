// Add by PP for focus 2020-01-20 start
window.onload = function () { 
        document.getElementById('screen_id').focus();
}
// Add by PP for focus 2020-01-31 end
//サブミット
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
