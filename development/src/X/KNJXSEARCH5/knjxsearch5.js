function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function search_submit(mode){

    var flg = false;
    for (var i=0;i<document.forms[0].elements.length;i++){
        var e = document.forms[0].elements[i];
        if ((e.type == 'text' || e.type == 'select-one') && e.value != ''){
            flg = true;
            break;
        }
    }
    if (!flg) {
        alert('{rval MSG301}' + '最低一項目を指定してください。');
        return true;
    }
    parent.left_frame.search(document.forms[0],mode);
    return false;
}
function btn_back(){
    parent.right_frame.location.href = parent.left_frame.document.forms[0].path.value + '&init=1';
}
