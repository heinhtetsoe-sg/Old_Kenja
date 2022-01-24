function btn_submit(cmd) {    
    if (cmd == "delete") {
        if(document.forms[0].SEQ.value == ""){
            alert('{rval MSG304}');
            return;
        } else {
           if(!confirm('{rval MSG103}')){
            return;
           }
        }
    }
    if (cmd == 'reset'){
        if(document.forms[0].SEQ.value == ""){
            alert('{rval MSG304}');
            return;
        } else {
           if(!confirm('{rval MSG106}')){
            return;
           }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

//テキスト内でEnterを押してもsubmitされないようにする
function btn_keypress(){
    if (event.keyCode == 13){
        event.keyCode = 0;
        window.returnValue  = false;
    }
}
