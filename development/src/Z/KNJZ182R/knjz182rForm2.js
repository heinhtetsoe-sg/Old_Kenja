function btn_submit(cmd) {
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
            return false;
    }
    if (cmd == 'delete') {
        if (document.forms[0].GRADE.value == ""){
            alert('{rval MSG304}' + '\n(学年)');
            return false;
        }

        if (!confirm('{rval MSG103}'))
            return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function level(cnt) {
    var level;
    level = document.forms[0].ASSESSLEVELCNT.value;
    if (level == cnt) {
        return false;
    }
    if (level == '') {
        document.forms[0].ASSESSLEVELCNT.value = 5;
    }

    document.forms[0].cmd.value = 'level';
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
            if (parseInt(that.value) < parseInt(document.forms[0]["RANK_LOW_" + level].value) && document.forms[0]["RANK_LOW_" + level].value != "") {
                alert('評定' + level +'の下限の数値を下回っています。');
                that.value = "";
                return false;
            }

            if (parseInt(that.value) < parseInt(document.forms[0]["RANK_HIGH_" + underlevel].value) && document.forms[0]["RANK_HIGH_" + underlevel].value != "") {
                alert('評定' + underlevel +'の上限の数値を下回っています。');
                that.value = "";
                return false;
            }
            if (parseInt(that.value) > parseInt(document.forms[0]["RANK_HIGH_" + overlevel].value) && document.forms[0]["RANK_HIGH_" + overlevel].value != "") {
                alert('評定' + overlevel +'の上限の数値を上回っています。');
                that.value = "";
                return false;
            }
            //一段階上の下限値をセット
            setValue = that.value;
            setValue++;
            //hiddenの保持
            document.forms[0]["RANK_LOW_" + overlevel].value = setValue;
            //画面に表示
            document.getElementById('RANK_LOW_' + overlevel).innerHTML = setValue;
        }
    }
    return;
}

//上限値自動計算
function isNumbJyougen(that,level){
    var num;
    var setValue;
    
    that.value = toNumber(that.value);

    if (that.value < 0) {
        return;
    } else {
        if (that.value != "") {
            var underlevel = parseInt(level - 1);
/***
            if (parseInt(that.value) > parseInt(document.forms[0]["RANK_HIGH_" + level].value) && document.forms[0]["RANK_HIGH_" + level].value != "") {
                alert('評定' + underlevel +'の上限の数値を上回っています。');
                that.value = "";
                return false;
            }
            if (parseInt(that.value) <= 0) {
                alert('0より大きい値を指定して下さい。');
                that.value = "";
                return false;
            }
***/
            //一段階下の上限値をセット
            setValue = that.value;
            setValue--;
            //hiddenの保持
            document.forms[0]["RANK_HIGH_" + underlevel].value = setValue;
            //画面に表示
            document.getElementById('RANK_HIGH_' + underlevel).innerHTML = setValue;
        }
    }
    return;
}
