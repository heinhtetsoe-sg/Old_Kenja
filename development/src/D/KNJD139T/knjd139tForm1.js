function btn_submit(cmd){

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }

    if (cmd == 'committee'){     //委員会選択
        loadwindow('knjd139tindex.php?cmd=committee',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);
        return true;
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        //更新中の画面ロック(全フレーム)
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }
    if (cmd == 'update') {
        //更新ボタン・・・読み込み中は、更新ボタンをグレー（押せないよう）にする。
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_up_next.disabled = true;
        document.forms[0].btn_up_pre.disabled = true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//テキスト内でEnterを押してもsubmitされないようにする
//Submitしない
function btn_keypress() {
    if (event.keyCode == 13) {
        event.keyCode = 0;
        window.returnValue  = false;
    }
}
