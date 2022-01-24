function btn_submit(cmd){

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }

    if (cmd == 'club'){                 //部活動選択
        loadwindow('knjd132hindex.php?cmd=club',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);
        return true;
    } else if (cmd == 'committee'){     //委員会選択
        loadwindow('knjd132hindex.php?cmd=committee',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);
        return true;
    } else if (cmd == 'qualified'){     //検定選択
        var standard_w = 880;
        var standard_h = 550;

        var w = standard_w;
        if (window.innerWidth === undefined) {
        } else {
            w = parseInt((window.innerWidth * 0.85), 10)
        }
        var h = 550;
        if (window.innerHeight === undefined) {
        } else {
            h = parseInt((window.innerHeight * 0.85), 10)
        }
        w_size = (w > standard_w) ? standard_w : w;
        h_size = (h > standard_h) ? standard_h : h;
        loadwindow('knjd132hindex.php?cmd=qualified',0,document.documentElement.scrollTop || document.body.scrollTop,w_size,h_size);
        return true;
    } else if (cmd == 'club_record'){  //記録備考選択
        loadwindow('knjd132hindex.php?cmd=club_record',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);
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
