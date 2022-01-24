function btn_submit(cmd) {

    if ((cmd == 'delete' || cmd == 'delete2') && !confirm('{rval MSG103}')){
        return true;
    }else if (cmd == 'delete' || cmd == 'delete2'){
        for (var i=0; i < document.forms[0].elements.length; i++)
        {
            if (document.forms[0].elements[i].name == "CHECKED[]" && document.forms[0].elements[i].checked){
                break;
            }
        }
        if (i == document.forms[0].elements.length){
            alert("チェックボックスを選択してください");
            return true;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
var selectedRow = 0;
function selectRow() {
    if (event.srcElement.parentElement.rowIndex == null) {
        return;
    }
    list.rows[selectedRow].bgColor = "white";
    selectedRow = event.srcElement.parentElement.rowIndex;
    list.rows[selectedRow].bgColor = "#ccffcc";
    
    var chk = document.forms[0]["CHECKED\[\]"];
    if (chk.length){
        parent.bottom_frame.location.href = "knje062index.php?cmd=edit&CHECKED="+chk[selectedRow].value;
    }else if (chk){
        parent.bottom_frame.location.href = "knje062index.php?cmd=edit&CHECKED="+chk.value;
    }
}
function check_all(obj){
    for (var i=0; i < document.forms[0].elements.length; i++)
    {
        if (document.forms[0].elements[i].name == "CHECKED[]"){
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}
