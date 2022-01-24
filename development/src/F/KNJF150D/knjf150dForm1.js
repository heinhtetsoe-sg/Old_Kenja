function btn_submit(cmd)
{
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')){
            return true;
        } else {
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
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//全チェック操作
function check_all(obj){
    for (var i=0; i < document.forms[0].elements.length; i++)
    {
        if (document.forms[0].elements[i].name == "CHECKED[]"){
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

function jumpClick() {
    document.forms[0].btn_jump.onclick();
    return false;
}

function closing_window(no)
{
    var msg;
    if(no == 'year'){
        msg = '{rval MSG305}';
    }else if(no == 'cm'){
        msg = '{rval MSG300}';
    }else if(no == 'sf'){
        msg = '職員データを取得できませんでした。';
    }
    alert(msg);
    closeWin();
    return true;
}
