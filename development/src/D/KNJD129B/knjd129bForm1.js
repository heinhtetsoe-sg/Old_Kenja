function btn_submit(cmd) {

    if(cmd == 'show_all'){
        document.forms[0].shw_flg.value = (document.forms[0].shw_flg.value == 'on')? 'off' : 'on';
        cmd = '';
    }else if(cmd == ''){
        document.forms[0].shw_flg.value = 'off';
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    } else if (cmd == 'update') {
        //更新権限チェック
        userAuth = document.forms[0].USER_AUTH.value;
        updateAuth = document.forms[0].UPDATE_AUTH.value;
        if (userAuth < updateAuth){
            alert('{rval MSG300}');
            return false;
        }
        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            if (e.type == 'text' && e.value != '') {
                //スペース削除
                var str_num = e.value;
                e.value = str_num.replace(/ |　/g,"");
                var nam = e.name;
                //数字チェック
                if (isNaN(e.value)) {
                    alert('{rval MSG907}');
                    return false;
                }
                //満点チェック
                var perfectName   = nam.split("-")[0] + "_PERFECT";
                var perfectNumber = nam.split("-")[1];
                perfectObject = eval("document.forms[0][\"" + perfectName + "-" + perfectNumber + "\"]");
                var perfect = parseInt(perfectObject.value);
                if (!isNaN(e.value) && (e.value > perfect || e.value < 0)) {
                    alert('{rval MSG901}' + '\n0～'+perfect+'まで入力可能です');
                    return false;
                }
            }
        }
        clickedBtnUdpate(true);
    }

    //更新ボタン・・・読み込み中は、更新ボタンをグレー（押せないよう）にする。
    document.forms[0].btn_calc.disabled = true;
    document.forms[0].btn_update.disabled = true;
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLock();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//更新時、サブミットする項目使用不可
function clickedBtnUdpate(disFlg) {
    if (disFlg) {
        document.forms[0].H_GRADE.value = document.forms[0].GRADE.value;
        document.forms[0].H_SEMESTER.value = document.forms[0].SEMESTER.value;
        document.forms[0].H_TESTKIND.value = document.forms[0].TESTKIND.value;
        document.forms[0].H_CLASSCD.value = document.forms[0].CLASSCD.value;
        document.forms[0].H_SUBCLASSCD.value = document.forms[0].SUBCLASSCD.value;
    } else {
        document.forms[0].GRADE.value = document.forms[0].H_GRADE.value;
        document.forms[0].SEMESTER.value = document.forms[0].H_SEMESTER.value;
        document.forms[0].TESTKIND.value = document.forms[0].H_TESTKIND.value;
        document.forms[0].CLASSCD.value = document.forms[0].H_CLASSCD.value;
        document.forms[0].SUBCLASSCD.value = document.forms[0].H_SUBCLASSCD.value;
    }

    document.forms[0].GRADE.disabled = disFlg;
    document.forms[0].SEMESTER.disabled = disFlg;
    document.forms[0].TESTKIND.disabled = disFlg;
    document.forms[0].CLASSCD.disabled = disFlg;
    document.forms[0].SUBCLASSCD.disabled = disFlg;
    document.forms[0].btn_reset.disabled = disFlg;
    document.forms[0].btn_end.disabled = disFlg;
    document.forms[0].btn_show.disabled = disFlg;
    document.forms[0].btn_calc.disabled = disFlg;
}

function calc(obj) {
    //スペース削除
    var str_num = obj.value;
    obj.value = str_num.replace(/ |　/g,"");
    var str = obj.value;
    var nam = obj.name;

    //数字チェック
    if (isNaN(obj.value)){
        alert('{rval MSG907}');
        obj.value = obj.defaultValue;
        return;
    }
    //満点チェック
    var perfectName   = nam.split("-")[0] + "_PERFECT";
    var perfectNumber = nam.split("-")[1];
    perfectObject = eval("document.forms[0][\"" + perfectName + "-" + perfectNumber + "\"]");
    var perfect = parseInt(perfectObject.value);

    var score = parseInt(obj.value);
    if (score > perfect) {
        alert('{rval MSG914}'+'0点～'+perfect+'点以内で入力してください。');
        obj.value = obj.defaultValue;
        return;
    }

    var score = parseInt(obj.value);
    if (score < 0) {
        alert('{rval MSG914}'+'0点～'+perfect+'点以内で入力してください。');
        obj.value = obj.defaultValue;
        return;
    }

    //手入力で変更した場合、調査用をブランクにする
    if (obj.value != obj.defaultValue) {
        var scoName   = nam.split("-")[0] + "_SCORE";
        var scoNumber = nam.split("-")[1];
        scoObject = eval("document.forms[0][\"" + scoName + "-" + scoNumber + "\"]");
        scoObject.value = "";
        return;
    }
}
