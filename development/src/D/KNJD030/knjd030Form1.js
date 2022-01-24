function getGtreData(gtredata){
    document.forms[0].GTREDATA.value = gtredata;
    document.forms[0].cmd.value = 'main';
    document.forms[0].submit();
    return false;
}
function btn_submit(cmd) {

    if (cmd == 'cancel' && !confirm('{rval MSG106}')){
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function calc(obj, perfect){
    var sum = 0;
    var exam = 0;
    
    obj.value = toInteger(obj.value);
    if ((obj.value != '' && !isNaN(obj.value)) && (parseInt(obj.value) > perfect)){
        alert('得点は'+perfect+'点以下で入力して下さい。');
        obj.value = '';
    }
    var Score = document.forms[0]['SCORE\[\]'];
    for (var i = 0;i < Score.length; i++)
    {
        if (Score[i].value != '' && !isNaN(Score[i].value)){
            sum += parseInt(Score[i].value);
            exam++;
        }
    }
    if (exam > 0){
        outputLAYER('AVG_SCORE' , Math.round((sum/exam)*10)/10.0);
        outputLAYER('EXAMINEE' , exam);
    } else {
        outputLAYER('AVG_SCORE' , 0);
        outputLAYER('EXAMINEE' , 0);
    }       
}
