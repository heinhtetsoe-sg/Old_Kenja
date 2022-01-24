function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }
    
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            //更新中の画面ロック
            updateFrameLock();
        }
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_select_staff(counter, year, cmd) {
    if (cmd == 'select_staff'){
        loadwindow('knjb0110index.php?cmd=select_staff&YEAR='+year+'&Counter='+counter, event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 450);
        return true;
    }
}