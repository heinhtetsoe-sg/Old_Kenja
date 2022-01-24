function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function scrollRC() {
    document.getElementById("trow").scrollLeft = document.getElementById("tbody").scrollLeft;
    document.getElementById("tcol").scrollTop = document.getElementById("tbody").scrollTop;
}
