function btn_submit(cmd) {
return false;//この区間は通らないと思う
    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].left_select.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_all(obj) {
    var ii = 0;
    re = new RegExp("RCHECK");
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (String(document.forms[0].elements[i].name).match(re)) {
            document.forms[0].elements[i].checked = obj.checked;
            ii++;
        }
    }
}

function doSubmit(cmd) {
//    //必須チェック
//    if (document.forms[0].RCHECK0.checked && document.forms[0].REMARK.value =="") {
//        alert ('{rval MSG301}');
//        return false;
//    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].left_select.length==0 && document.forms[0].right_select.length==0) {
        alert('{rval MSG916}');
        return false;
    }
    for (var i = 0; i < document.forms[0].left_select.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//備考チェックボックス
function CheckRemark() {
    if (document.forms[0].NO_COMMENTS.checked == true) {
        document.forms[0].REMARK.value = document.forms[0].NO_COMMENTS_LABEL.value;
        document.forms[0].REMARK.disabled =true;
    } else {
        document.forms[0].REMARK.disabled = false;
    }
}
