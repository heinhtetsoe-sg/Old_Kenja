function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//上限値自動計算
function isNumb(that,level){
    var num;
    var anser;
    
    that.value = toNumber(that.value);

    if(that.value <= 0){
        return;
    }else{
        anser = (that.value - 1);
        document.getElementById('ASSESSHIGH_ID' + level).innerHTML = anser;
        AssesslowObject  = eval("document.forms[0].Assesshightvalue" + level);
        AssesslowObject.value  = anser;        
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