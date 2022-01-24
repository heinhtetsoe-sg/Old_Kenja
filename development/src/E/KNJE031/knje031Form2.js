function btn_submit(cmd) {
    //削除
    if(cmd == 'delete' && !confirm('{rval MSG103}')) { 
        return true;
    }
    //取消
    if(cmd == 'reset' && !confirm('{rval MSG106}')) { 
        return false;
    }
    //更新中の画面ロック(全フレーム)
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function selcheck(that) {

    //全角から半角
    that.value = toHankakuNum(that.value);
    //数値型へ変換
    that.value = toInteger(that.value);
    //セルが空の時０
    if(that.value == '' ){
        that.value = 0;
        return;
    }
}

//区分別テキストボックス内禁止処理
function check(that) {

    checktest(that.value);

}

function checktest(num) {

    switch (num) {
        case "1":
            document.forms[0].TRANSFER_EDATE.disabled = false;
            document.forms[0].TRANSFERPLACE.disabled = false;
            document.forms[0].TRANSFERADDR.disabled = false;
            document.forms[0].ABROAD_CLASSDAYS.disabled = false;
            document.forms[0].ABROAD_CREDITS.disabled = false;
            document.all('TRANSFER_EDATE').style.backgroundColor = "white";
            document.all('TRANSFERPLACE').style.backgroundColor = "white";
            document.all('TRANSFERADDR').style.backgroundColor = "white";
            document.all('ABROAD_CLASSDAYS').style.backgroundColor = "white";
            document.all('ABROAD_CREDITS').style.backgroundColor = "white";
            break;
        case "2":
        case "3":
            document.forms[0].TRANSFER_EDATE.disabled = false;
            document.forms[0].TRANSFERPLACE.disabled = true;
            document.forms[0].TRANSFERADDR.disabled = true;
            document.forms[0].ABROAD_CLASSDAYS.disabled = true;
            document.forms[0].ABROAD_CREDITS.disabled = true;
            document.all('TRANSFER_EDATE').style.backgroundColor = "white";
            document.all('TRANSFERPLACE').style.backgroundColor = "darkgray";
            document.all('TRANSFERADDR').style.backgroundColor = "darkgray";
            document.all('ABROAD_CLASSDAYS').style.backgroundColor = "darkgray";
            document.all('ABROAD_CREDITS').style.backgroundColor = "darkgray";
            break;
        case "4":
            document.forms[0].TRANSFER_EDATE.disabled = true;
            document.forms[0].TRANSFERPLACE.disabled = true;
            document.forms[0].TRANSFERADDR.disabled = true;
            document.forms[0].ABROAD_CLASSDAYS.disabled = true;
            document.forms[0].ABROAD_CREDITS.disabled = true;
            document.all('TRANSFER_EDATE').style.backgroundColor = "darkgray";
            document.all('TRANSFERPLACE').style.backgroundColor = "darkgray";
            document.all('TRANSFERADDR').style.backgroundColor = "darkgray";
            document.all('ABROAD_CLASSDAYS').style.backgroundColor = "darkgray";
            document.all('ABROAD_CREDITS').style.backgroundColor = "darkgray";
            break;
    }
}
