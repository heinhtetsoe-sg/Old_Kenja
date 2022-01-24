function btn_submit(cmd) {
    if (cmd=="clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        } 
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function level(cnt) {
    
    var level;
    level = document.forms[0].ASSESSLEVELCNT.value;
    if(level == cnt){
        return false;
    }
    if (level > 100) {
        alert('{rval MSG913}'+'評価段階数は100を超えてはいけません。');
        return false;
    } 
    
    document.forms[0].cmd.value = 'level';
    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    result = confirm('{rval MSG107}');
    if (result == false) {
        return false;
    }
}

function isNumb(that,level,mode){
    var num;
    var anser;

    that.value = toNumber(that.value);

    if(that.value <= 0){
        return;
    }else{
        if(mode == 'ABCD'){
            anser = that.value;
            anser = ((anser * 10) - 1) / 10;
            anser = "" + anser;
            if(anser.length == 1)
            {
                anser = anser + '.0';
            }
        }else{
            anser = (that.value - 1);
        }
        document.all['strID' + (level)].innerHTML = anser;
    }
    return;
}

function closing_window(){
        alert('{rval MSG300}');
        closeWin();
        return true;
}
