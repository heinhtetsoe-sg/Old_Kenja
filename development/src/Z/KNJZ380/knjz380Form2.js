//サブミット
function btn_submit(cmd) {

    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function EnableBtns(){
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_del.disabled = true;
}

function closing_window(){
    alert('{rval MSG300}');
    closeWin();
}

function check_Val(that){
    that.value = toInteger(that.value);
    return false;
}
function doSubmit(cmd) {
    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function checktest(val) {
    if (val == '01' || val == '02') {
        document.forms[0].TESTITEMCD.disabled = true;
        document.forms[0].TESTITEMCD.value = '01';
    } else {
        document.forms[0].TESTITEMCD.disabled = false;
        document.forms[0].TESTITEMCD.value = '';
    }
}
