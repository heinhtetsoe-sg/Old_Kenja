function btn_submit(cmd) {
    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}'))
            return false;
    }

    //削除
    if (cmd == 'delete' && !confirm('{rval MSG103}')) {
        return false;
    }

    //確定
    if (cmd == 'kakutei') {
        if (!document.forms[0].PERFECT.value || !document.forms[0].ASSESSLEVEL_CNT.value) {
            alert('{rval MSG301}'+'\n'+'満点または段階数を入力してください。');
            return false;
        }
    }

    //更新
    if (cmd == 'update' || cmd == 'add') {
        var flg = true;
        for (var i=0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            if (e.name.match(/^ASSESSLOW/) && !e.value.length) {
                flg = false;
            }
        }
        if (!flg) {
            alert('{rval MSG301}'+'\n'+'( 下限値 )');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//数値チェック（パターンコード・満点・段階数）
function checkNum(obj) {
    obj.value=toInteger(obj.value);
    if (obj.value.length > 0 && obj.value == 0) {
        alert('{rval MSG901}'+'\n'+'1より大きい値を入力してください。');
        obj.focus();
        obj.select();
    }
}

//数値チェック（下限値）
function checkNum2(obj) {
    //数値チェック＆変換
    obj.value = toInteger(obj.value);
    if (obj.value.length) {
        obj.value = parseInt(obj.value, 10);
    }

    var nm  = obj.name.split('-');
    this_cnt = nm[1];
    pre_cnt = nm[1] - 1;

    var perfect = document.forms[0].PERFECT.value;
    var cnt = document.forms[0].ASSESSLEVEL_CNT.value;

    //直前の下限値取得
    downAssessLow = '';
    for (var i=pre_cnt; i > 0; i--) {
        if (!downAssessLow.length) {
            downAssessLow = document.forms[0]['ASSESSLOW-'+i].value;
        }
    }

    //直後の下限値取得
    upAssessLow = '';
    for (var i=this_cnt; i <= cnt; i++) {
        if (!upAssessLow.length && i != this_cnt) {
            upAssessLow = document.forms[0]['ASSESSLOW-'+i].value;
        }
    }

    //数値変換
    if (downAssessLow.length) {
        downAssessLow = parseInt(downAssessLow, 10);
    }
    if (upAssessLow.length) {
        upAssessLow = parseInt(upAssessLow, 10);
    }
    if (perfect.length) {
        perfect = parseInt(perfect, 10);
    }

    //値チェック
    errflg1 = false;
    if (perfect && obj.value && perfect < obj.value) {
        errflg1 = true;
    }
    errflg2 = false;
    if (downAssessLow && obj.value && downAssessLow >= obj.value) {
        errflg2 = true;
    }
    errflg3 = false;
    if (upAssessLow && obj.value && upAssessLow <= obj.value) {
        errflg3 = true;
    }

    //エラーメッセージ
    if (errflg1 || errflg2 || errflg3) {
        if (errflg1) {
            alert('{rval MSG901}'+'\n満点( '+perfect+' )より小さい値を入力してください。');
        } else if (downAssessLow && upAssessLow) {
            msgDownLow = downAssessLow + 1;
            msgUpLow = upAssessLow - 1;
            alert('{rval MSG901}'+'\n'+msgDownLow+'～'+msgUpLow+'を入力してください。');
        } else if (errflg2) {
            alert('{rval MSG901}'+'\n'+downAssessLow+'より大きい値を入力してください。');
        } else {
            alert('{rval MSG901}'+'\n'+upAssessLow+'より小さい値を入力してください。');
        }
        obj.focus();
        obj.select();
        return;
    }

    //上限値
    if (obj.value) {
        document.forms[0]['ASSESSHIGH-'+pre_cnt].value = obj.value - 1;
        document.getElementById('ASSESSHIGH-'+pre_cnt).innerHTML = obj.value - 1;
    } else {
        document.forms[0]['ASSESSHIGH-'+pre_cnt].value = "";
        document.getElementById('ASSESSHIGH-'+pre_cnt).innerHTML = "";
    }
}

//画面の切替
function Page_jumper(link) {
    if (confirm('{rval MSG108}')) {
        parent.location.href=link;
    }
}
