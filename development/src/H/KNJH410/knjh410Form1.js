function btn_submit(cmd) {

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG308}');
        return true;
    }

    if (cmd == 'delete') {
        var flag;
        flag = "";
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            if (e.type == "checkbox" && e.checked && e.name == "DELCHK[]"){
                var val = e.value;
                if (val != ''){
                    flag = "on";
                }
            }
        }
        if (flag == ''){
            alert("チェックボックスが選択されておりません。");
            return;
        }
        if (confirm('{rval MSG103}')) {
        } else {
            return;
        }
    }

	//追加ボタン
    if (cmd == 'insert'){
	    param = document.forms[0].SCHREGNO.value;
        auth = document.forms[0].auth2.value;
        loadwindow('../../H/KNJH410_ACTION_DOCUMENT/knjh410_action_documentindex.php?cmd=insertSub&SEND_PRGID=KNJH410&SEND_AUTH='+auth+'&SCHREGNO='+param,0,0,600,450);
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_all(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "DELCHK[]" && !document.forms[0].elements[i].disabled) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

function openGamen(root, param, subwin, x, y) {
    url = root;
    prm = param;
    subwin = subwin;
    wopen(url + prm, subwin, x, y, screen.availWidth/2, screen.availHeight/2);

}
