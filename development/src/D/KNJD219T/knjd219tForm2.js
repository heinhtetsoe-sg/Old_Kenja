function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    } else if (cmd == 'update') {
        if (document.forms[0]["ASSESSLOW_2"].value == "" || document.forms[0]["ASSESSHIGH_2"].value == "" || document.forms[0]["ASSESSHIGH_3"].value == "" || document.forms[0]["ASSESSHIGH_4"].value == "") {
            alert('全ての段階値の範囲を入力して下さい。');
            return false;
        }
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//下限値自動計算
function isNumb(that,level){
    var num;
    var setValue;
    
    that.value = toNumber(that.value);

    if (that.value < 0) {
        return;
    } else {
        if (that.value != "") {
            //値チェック
            var underlevel = parseInt(level - 1);
            var overlevel = parseInt(level + 1);
            if (parseInt(that.value) < parseInt(document.forms[0]["ASSESSLOW_" + level].value) && document.forms[0]["ASSESSLOW_" + level].value != "") {
                alert('評定' + level +'の下限の数値を下回っています。');
                that.value = "";
                return false;
            }

            if (parseInt(that.value) < parseInt(document.forms[0]["ASSESSHIGH_" + underlevel].value) && document.forms[0]["ASSESSHIGH_" + underlevel].value != "") {
                alert('評定' + underlevel +'の上限の数値を下回っています。');
                that.value = "";
                return false;
            }
            if (parseInt(that.value) > parseInt(document.forms[0]["ASSESSHIGH_" + overlevel].value) && document.forms[0]["ASSESSHIGH_" + overlevel].value != "") {
                alert('評定' + overlevel +'の上限の数値を上回っています。');
                that.value = "";
                return false;
            }
            //一段階上の下限値をセット
            setValue = that.value;
            setValue++;
            //hiddenの保持
            document.forms[0]["ASSESSLOW_" + overlevel].value = setValue;
            //画面に表示
            document.getElementById('ASSESSLOW_' + overlevel).innerHTML = setValue;
        }
    }
    return;
}

//上限値自動計算(評定1のみ)
function isNumbJyougen(that,level){
    var num;
    var setValue;
    
    that.value = toNumber(that.value);

    if (that.value < 0) {
        return;
    } else {
        if (that.value != "") {
            var underlevel = parseInt(level - 1);
            if (parseInt(that.value) > parseInt(document.forms[0]["ASSESSHIGH_" + level].value) && document.forms[0]["ASSESSHIGH_" + level].value != "") {
                alert('評定' + underlevel +'の上限の数値を上回っています。');
                that.value = "";
                return false;
            }
            if (parseInt(that.value) <= 0) {
                alert('0より大きい値を指定して下さい。');
                that.value = "";
                return false;
            }
            
            //一段階下の上限値をセット
            setValue = that.value;
            setValue--;
            //hiddenの保持
            document.forms[0]["ASSESSHIGH_" + underlevel].value = setValue;
            //画面に表示
            document.getElementById('ASSESSHIGH_' + underlevel).innerHTML = setValue;
        }
    }
    return;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

function viewCheck()
{
    alert('学年観点評価設定が完了していません。設定を見直して下さい');
}

function OnTestCntError() {
    alert('年間試験設定が完了していないため、累積別の設定はできません。\n管理者コントロールにて年間試験設定を行って下さい。');
}
