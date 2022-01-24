function btn_submit(cmd) {
    if (cmd=="clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        } 
    } else if (cmd == "copy") {
        if (document.forms[0].PRE_YEAR_CNT.value <= 0) {
            alert('前年度のデータが存在しません。');
            return false;
        }
        if (document.forms[0].THIS_YEAR_CNT.value > 0) {
            if (!confirm('今年度のデータは破棄されます。コピーしてもよろしいですか？')) {
                return false;
            }
        } else {
            if (!confirm('{rval MSG101}')) {
                return false;
            }
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
        anser = (that.value - 1);
        document.all['strID' + (level)].innerHTML = anser;
    }
    return;
}

function closing_window(){
        alert('{rval MSG300}');
        closeWin();
        return true;
}
