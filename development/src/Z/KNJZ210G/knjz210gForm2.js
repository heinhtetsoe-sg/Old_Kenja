function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    //�T�u�~�b�g
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//����l�����v�Z
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
