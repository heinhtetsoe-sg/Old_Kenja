function btn_submit(cmd) {
    
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;

}

//親画面をサブミットしてから閉じる
function btn_back()
{
    top.window.opener.btn_submit('edit');
    closeWin();
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

//路線選択
function Page_jumper2() {
    var requestroot = document.forms[0].REQUESTROOT.value;

    var cmd    = "rosen";
    var haba   = 350;
    var takasa = 450;

    link = requestroot+"/Z/KNJZ091A_3/knjz091a_3index.php?cmd="+cmd;
    loadwindow(link, 0, 200, haba, takasa);
}
