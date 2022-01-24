function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(KNJZ210E){

    action = document.forms[0].action;
    target = document.forms[0].target;

    //document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].action = "/Z/KNJZ210E/index.php";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//上限値の最大値をセット
function setAssesshigh(that,level,flg){
    var num;
    var anser;
    
    that.value = toNumber(that.value);

    if(that.value <= 0){
        return;
    }else{
        anser = that.value * flg;
        document.getElementById('ASSESSHIGH_ID' + level).innerHTML = anser;
        AssesslowObject  = eval("document.forms[0].Assesshightvalue" + level);
        AssesslowObject.value  = anser;        
    }
    return;
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

