function btn_submit(cmd) {
    //編集中フラグON
    if (cmd == 'add' || cmd == 'ins' || cmd == 'del' || cmd == 'extend' || cmd == 'moveUp' || cmd == 'moveDown') {
        setupFlgOn();
    } else {
        //編集中かどうかの確認
        if (cmd != 'update' && !setupFlgCheck()) return;
    }
    //初期値
    if (cmd == 'def') {
        dataDiv = document.forms[0].DATA_DIV.value;
        gradeHrClass = document.forms[0].GRADE_HR_CLASS.value;
        subclassCd = document.forms[0].SUBCLASSCD.value;
        if (dataDiv == '' || gradeHrClass == '' || subclassCd == '') {
            alert('教科名を選択して下さい。');
            return;
        }
        inputseqDataCnt = document.forms[0].INPUTSEQ_DATA_CNT.value;
        msg = "";
        if (inputseqDataCnt > 0) {
            msg += "【テスト単元観点別評価・配点設定】が存在します。\n";
        }
        msg += "【単元（学校用）】";
        if (!confirm(msg + 'を読込みます。宜しいですか？')) {
            return;
        }
    }
    //編集中フラグOFF
    if (cmd == 'reset' || cmd == 'def') {
        setupFlgOff();
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//スクロール
function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}

//編集フラグ 1:編集中
function setupFlgOn() {
    document.forms[0].setupFlg.value = "1";
}
function setupFlgOff() {
    document.forms[0].setupFlg.value = "";
}
function setupFlgCheck() {
    setupFlg = document.forms[0].setupFlg.value;
    if (setupFlg == "1" && !confirm('{rval MSG108}')) {
        return false;
    }
    setupFlgOff();
    return true;
}

//終了
function btnEnd() {
    if (!setupFlgCheck()) return;
    closeWin();
}

//選択
function rankClick(obj) {
    targetVal = obj.value;

    //選択(指定行)・・・複数選択は不可のため、指定行以外のチェックをはずす。
    for (var i = 0; i < document.forms[0].elements.length; i++ ) {
        var e = document.forms[0].elements[i];
        type = e.type;
        name = e.name;
        val = e.value;
        if (type == 'checkbox' && name.match(/RANK./)) {
            if (val != targetVal) e.checked = false;
        }
    }
}
