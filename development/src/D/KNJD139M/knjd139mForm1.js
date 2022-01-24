function btn_submit(cmd) {
    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //更新
    if (cmd == 'update') {
        if (!document.forms[0].SCHREGNO.value) {
            alert('{rval MSG304}');
            return;
        }
    }

    //総合的な学習の時間
    if (cmd == 'totalStudy') {
        var attBttn = document.forms[0].btn_totalStudy.getBoundingClientRect();
        var setY    = attBttn.top + window.pageYOffset + 20;
        loadwindow('knjd139mindex.php?cmd=totalStudy', 0, setY, 500, 200);
        return true;
    }

    //出欠備考参照
    if (cmd == 'attendRemark') {
        var attBttn = document.forms[0].btn_attendRemark.getBoundingClientRect();
        var setY    = attBttn.top + window.pageYOffset - 290;
        loadwindow('knjd139mindex.php?cmd=attendRemark', 0, setY, 500, 200);
        return true;
    }

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
//入力チェック(観点、行動の記録)
function checkRecord(obj, div) {
    if (obj.value == "") {
        return true;
    }
    var idList  = document.forms[0]["HID_RECORDLIST" + div].value.split(",");
    var findFlg = false;
    var setStr  = '';
    var sep     = '';
    for (i = 0;i < idList.length;i++) {
        setStr += sep + idList[i];
        sep = ', ';
        if (idList[i] == obj.value) {
            findFlg = true;
        }
    }
    if (!findFlg) {
        alert('{rval MSG901}' + '\n「' + setStr + '」を入力して下さい。');
        obj.focus();
        obj.select();
        return false;
    }
    return true;
}
//Enterキーで移動
function nextGo(obj, idx) {
    var name      = obj.name;
    var recordArr = document.getElementsByClassName("RECORD");
    var remarkArr = document.getElementsByClassName("REMARK1");

    // Ent13
    var e = window.event;
    if (e.keyCode != 13) {
        return;
    }

    if (window.event.shiftKey) {
        idx = idx - 2;
    }

    if (name.match(/REMARK1_./)) {
        if (remarkArr[idx]) {
            remarkArr[idx].focus();
            remarkArr[idx].select();
        } else if (!window.event.shiftKey) {
            if (recordArr[0]) {
                recordArr[0].focus();
                recordArr[0].select();
            }
        }
    }

    if (name.match(/RECORD./)) {
        if (recordArr[idx]) {
            recordArr[idx].focus();
            recordArr[idx].select();
        } else if (window.event.shiftKey) {
            var finIdx = 0;
            for (var i = 0; i < remarkArr.length; i++) {
                finIdx++;
            }
            if (remarkArr[finIdx - 1]) {
                remarkArr[finIdx - 1].focus();
                remarkArr[finIdx - 1].select();
            }
        }
    }
    return;
}
