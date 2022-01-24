function btn_submit(cmd)
{
    if(cmd == 'sim' || cmd == 'decision'){
        if(document.forms[0].TESTDIV.value == ''){
            alert('{rval MSG301}' + '\n ( 入試区分 )');
            return false;
        }
    }
    if ((cmd == 'sim' || cmd == 'decision') && !confirm('{rval MSG101}')){
        return true;
    }
//    if (cmd == 'decision' && !confirm('{rval MSG101}\n\n２科目で行う場合は前回の４科目での判定は無効になります。\n処理後に再度４科目を判定して下さい。')){
//        return true;
//    }
//    if (cmd == 'decision'){
//        if(!confirm('{rval MSG101}\n\n２科目で行う場合は前回の４科目での判定は無効になります。\n処理後に再度４科目を判定して下さい。')){
//            return true;
//        }
//    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function chg_value()
{
    document.forms[0].btn_decision.disabled = true;
}
function chg_rate()
{
    var cnt = document.forms[0].SUCCESS_CNT.value;
    var rate = document.forms[0].BACK_RATE.value;
    if (!isNaN(cnt) && cnt != "" && !isNaN(rate) && rate != "" ){
        outputLAYER("CAPA_CNT",Math.floor(cnt*rate/100)+"　名");
        document.forms[0].CAPA_CNT.value = Math.floor(cnt*rate/100);
    }else{
        outputLAYER("CAPA_CNT","0　名");
    }
}