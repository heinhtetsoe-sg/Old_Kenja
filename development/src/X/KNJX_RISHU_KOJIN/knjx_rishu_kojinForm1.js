function btn_submit(cmd){

    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closeMethod() {
    window.opener.btn_submit('main');
    closeWin();
}

//テキスト内でEnterを押してもsubmitされないようにする
function btn_keypress(){
    if (event.keyCode == 13){
        event.keyCode = 0;
        window.returnValue  = false;
    }
}

//テキスト内でBackspaceを押してもsubmitされないようにする
function btn_onkeydown(){
    if (event.keyCode == 8){
        event.keyCode = 0;
        window.returnValue  = false;
    }
}
