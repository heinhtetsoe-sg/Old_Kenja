function btn_submit(cmd) {
    if (cmd=="clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        } 
    }

    if (cmd=="copy") {
        result = confirm('{rval MSG102}');
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

    document.forms[0].cmd.value = 'level';
    document.forms[0].submit();
    return false;
}

function isNumb(that,level,mode){
    var num;
    var anser;

    that.value = toNumber(that.value);

    if (that.value <= 0) {
        return;
    } else {
        if (mode == 'SYOUSUU') {
            anser = that.value;
            anser = ((anser * 10) - 1) / 10;
            anser = "" + anser;
            if(anser.length == 1)
            {
                anser = anser + '.0';
            }
        } else {
            anser = (that.value - 1);
        }
        document.all['strID' + (level)].innerHTML = anser;
    }
    return;
}

