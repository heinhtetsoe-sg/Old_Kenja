function btn_submit(cmd) {
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }
    var groupDiv = document.forms[0].GROUPDIV.value;
    var divName = (groupDiv == "1") ? "受験" : "面接" ;

    if (cmd == 'update' || cmd == 'add') {
        if (document.forms[0].APPLICANTDIV.value == '') {
            alert('{rval MSG301}' + '\n(入試制度)');
            return false;
        }
        if (document.forms[0].TESTDIV.value == '') {
            alert('{rval MSG301}' + '\n(入試区分)');
            return false;
        }
        if (document.forms[0].GROUPCD.value == '') {
            alert('{rval MSG301}' + '\n(' + divName + '班コード)');
            return false;
        }
        if (document.forms[0].GROUPNAME.value == '') {
            alert('{rval MSG301}' + '\n(' + divName + '班名)');
            return false;
        }
        if (document.forms[0].GROUPPEOPLE.value == '') {
            
            alert('{rval MSG301}' + '\n(人数)');
            return false;
        }

    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
