function btn_submit(cmd) {
    //データ指定チェック
    if (cmd == 'update') {
        var dataCnt = document.forms[0].DATA_CNT.value;
        if (dataCnt == 0) {
            alert('{rval MSG304}');
            return false;
        }
    }

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//サブフォーム
function btn_submit_subform(cmd, counter, semester, chaircd, staffcdField) {
    var staffcd = document.forms[0][staffcdField].value;

    //科目担任選択ボタン押し下げ時
    if (cmd == 'substaff' || cmd == 'substaffProctor') {
        loadwindow(
            'knjb103cindex.php?cmd=' + cmd + '&Counter=' + counter + '&SEMESTER=' + semester + '&STAFF_CHAIRCD=' + chaircd + '&STAFF_STAFFCD=' + staffcd,
            event.clientX +
                (function () {
                    var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;
                    return scrollX;
                })(),
            event.clientY +
                (function () {
                    var scrollY = document.documentElement.scrollTop || document.body.scrollTop;
                    return scrollY;
                })(),
            650,
            450
        );
        return true;
    }
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

//スクロール
function scrollRC() {
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}

//子画面へ
function openKogamen(URL) {
    if (document.forms[0].SUBCLASSCD.value == '') {
        alert('科目を指定してください。');
        return;
    }
    var dataCnt = document.forms[0].DATA_CNT.value;
    if (dataCnt == 0) {
        alert('{rval MSG304}');
        return false;
    }
    if (!confirm('{rval MSG108}')) {
        return false;
    }

    document.location.href = URL;
    //    wopen(URL, 'SUBWIN', 0, 0, screen.availWidth, screen.availHeight);
}
