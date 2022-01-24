function btn_submit(cmd) {
    var staffdiv = document.forms[0].setStaffDiv.value;

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//結果反映
function refStaffName(stf) {
    for (var i=0; i < parent.document.forms[0].elements.length; i++) {
        if (parent.document.forms[0].elements[i].name == stf) {
            parent.document.forms[0].elements[i].value = document.forms[0].setStaffName.value;
        }
    }
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}
0