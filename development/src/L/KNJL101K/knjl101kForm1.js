function btn_submit(cmd)
{	
    if ((cmd == "sim" || cmd == "notice") && !confirm('{rval MSG101}')){
        return true;
    }
    if (cmd == 'decision' && !confirm('{rval MSG101}\n\n注意!! \n\n下位の学科・コースを確定後に上位の学科・コースを確定した場合は \nスライド合格に矛盾が生じる為、下位の学科・コースの判定は無効に \nなります。\n処理後に下位の学科・コースを再度処理して下さい。\n')){
        return true;
    }
	//2006.01.17 alp m-yama
	if (cmd == "decision" || cmd == "sim"){
		document.all('marq_msg').style.color = '#FF0000';
	}
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
//        outputLAYER("CAPA_CNT",Math.floor(cnt*rate/100)+"　名");
//        document.forms[0].CAPA_CNT.value = Math.floor(cnt*rate/100);

        outputLAYER("CAPA_CNT",Math.round(cnt*rate/100)+"　名");
        document.forms[0].CAPA_CNT.value = Math.round(cnt*rate/100);

    }else{
        outputLAYER("CAPA_CNT","0　名");
    }
}
