function btn_submit(cmd) {

    bodyHeight = (window.innerHeight || document.body.clientHeight || 0);
    document.forms[0].windowHeight.value = bodyHeight;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//画面リサイズ
function submit_reSize() {
    bodyHeight = (window.innerHeight || document.body.clientHeight || 0);

    document.getElementById("tbody").style.height = bodyHeight - 270;
}
