function btn_submit(cmd)
{
    if (cmd == 'reset'){
       if(!confirm('{rval MSG106}')){
        return;
       }
    }

    if (cmd == 'update') {
        //使用不可項目
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_reset.disabled = true;
        document.forms[0].btn_end.disabled = true;

        //リンクを使用不可
        var elem = document.getElementsByTagName("a");
        for(var i = 0; i < elem.length; ++i){
            elem[i].onclick = "return false;";
        }
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLock();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//親画面をサブミットしてから閉じる
function btn_back(cmd)
{
    window.opener.btn_submit('main');
    closeWin();
}

//子画面へ
function openSubWindow(URL) {
    wopen(URL, 'SUBWIN2', 0, 0, screen.availWidth, screen.availHeight);
}

//チェック制御
function check_change(obj, chkfin, chked_data, low_data) {

    var org = obj.name.split("_");
    for (var i=0; i < document.forms[0].elements.length; i++) {
        re = new RegExp("^SUBDATA_" + org[1]);
        var obj_updElement = document.forms[0].elements[i];
        if (obj_updElement.name.match(re)) {
            if (chkfin) {
                if (chked_data && obj.name == obj_updElement.name && !low_data) {
                    obj_updElement.checked = obj.checked;
                } else {
                    obj_updElement.checked = false;
                }
            } else if(obj.name == obj_updElement.name) {
                obj_updElement.checked = obj.checked;
            } else {
                obj_updElement.checked = false;
            }
        }
    }
}
