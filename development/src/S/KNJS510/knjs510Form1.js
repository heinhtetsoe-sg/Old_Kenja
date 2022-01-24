//サブミット
function btn_submit(cmd) {    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//行選択でリンクする
var selectedRow = 1;
function selectRow() {
    var list = document.getElementById('list');

    if (event.srcElement.parentElement.rowIndex == 0) {
        return false;
    }
    if (event.srcElement.parentElement.rowIndex == null) {
        return;
    }

    list.rows[selectedRow].bgColor = "white";
    selectedRow = event.srcElement.parentElement.rowIndex;
    if (list.rows[selectedRow].cells[0].firstChild.tagName != 'A') {
        return;
    }
    list.rows[selectedRow].bgColor = "#ccffcc";

    parent.right_frame.location.href = list.rows[selectedRow].cells[0].firstChild.href;
}
