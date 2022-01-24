//2013/01/15キーイベントタイムアウト処理をスルー
document.onLoad=keyThroughSet()

function btn_submit(cmd) {
    //更新チェック
    if (cmd == 'add' || cmd == 'chdel') {
        var schNos = document.forms[0].SCHREGNOS.value.split(',');
        var checkFlg = true;

        for (var i = 0; i < schNos.length; i++) {
            if (document.forms[0]['CHECK-' + schNos[i]].checked == true) {
                checkFlg = false;
            }
        }
        if (checkFlg) {
            alert('生徒を選択して下さい。');
            return;
        }
    }
    if (cmd == 'add') {
        disUpdate();
    }
    if (cmd == 'chdel') {
        if (!confirm('{rval MSG103}'))
            return false;
    }
    disSubmit();
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closing_window() {
    alert('{rval MSG300}'+'\n'+'管理者もしくは教科担当者のみ処理可能です。');
    closeWin();
    return true;
}
//全チェック処理
function checkAll(obj) {
    var schNos = document.forms[0].SCHREGNOS.value.split(',');

    for (var i = 0; i < schNos.length; i++) {
        document.forms[0]['CHECK-' + schNos[i]].checked = obj.checked;
    }
    return;
}
function datecheck(dval) {
    var chflg = 0;

    //Nullチェック
    if (dval == '') {
        return '';
    }
    //日付正規チェック
    if (!isDate2(dval)) {
       return '';
    }
    //日付の一致チェック
    if (dval == document.forms[0].DEFOULTDATE.value) {
        return '';
    }
    if (sem == 0){
        disSubmit();
        sem = 1;
        document.forms[0].cmd.value = 'dsub';
        document.forms[0].submit();
        return false;
    }
}

function check(obj) {
    if (getByte(obj.value) > 40){
        alert("全角２０、半角６０文字以内で入力してください。");
        obj.focus();
    }
}

function checkkey() {
    if (event.keyCode == 13){
        disSubmit();
        document.forms[0].cmd.value = 'add';
        document.forms[0].submit();
        return false;
    }
}

function confirmSonota(cmd) {
    if (!confirm("同日2回目の登録です。登録しますか？")) {
        document.forms[0].sonotaConfFlg.value ='';
        document.forms[0].sonotaNotChk.value ='';
        document.forms[0].SCHREGNO.value ='';
        return false;
    }
    disSubmit();
    document.forms[0].sonotaNotChk.value ='1'; // その他をチェックせずに更新する
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//データ変更した時、登録ボタン有効
function disObj(obj) {
    document.forms[0].btn_ok.disabled = false;
}
//読込中、登録ボタン無効（一時的）
function disSubmit() {
    document.forms[0].btn_ok.disabled = true;
}
//登録中、サブミットする項目無効（一時的）
function disUpdate() {
    //hidden保持
    document.forms[0].DIS_COURSE.value = document.forms[0].COURSE.value;
    document.forms[0].DIS_CHAIR.value = document.forms[0].CHAIR.value;
    document.forms[0].DIS_PERIOD.value = document.forms[0].PERIOD.value;
    document.forms[0].DIS_STAFF.value = document.forms[0].STAFF.value;
    document.forms[0].DIS_DATE.value = document.forms[0].DATE.value;
    //サブミット項目無効
    document.forms[0].COURSE.disabled = true;
    document.forms[0].CHAIR.disabled = true;
    document.forms[0].PERIOD.disabled = true;
    document.forms[0].STAFF.disabled = true;
    document.forms[0].DATE.disabled = true;
    document.forms[0].btn_calen.disabled = true;
}
