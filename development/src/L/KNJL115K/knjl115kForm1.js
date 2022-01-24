function btn_submit(cmd)
{
    if (cmd == 'update' && document.forms[0].STARTNUMBER.value == ""){
        alert('{rval MSG301}' + '\n（開始番号）');
        return true;
    }


//    if (cmd == 'update' && cmd == 'clear' && !confirm('{rval MSG101}\n')){
//        return true;
//    }
//2006/01/10
   if (cmd == 'update' || cmd == 'clear') {
       if (!confirm('{rval MSG101}')) {
           return false;
       }
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
        outputLAYER("CAPA_CNT",Math.floor(cnt*rate/100)+"　名");
        document.forms[0].CAPA_CNT.value = Math.floor(cnt*rate/100);
    }else{
        outputLAYER("CAPA_CNT","0　名");
    }
}

function passCountCtrl(obj)
{
    //合否区分ラジオボタン制御
    if(obj.value == 1){         //合格者
        document.forms[0].SUC_RADIO[0].checked = true;  //一般
        document.forms[0].SUC_RADIO[1].checked = false; //追加繰上
        document.forms[0].SUC_RADIO[2].checked = false; //附属推薦
        document.forms[0].STARTNUMBER.value = document.forms[0].max_success_noticeno.value

    }else if(obj.value == 2){   //不合格者
        document.forms[0].SUC_RADIO[0].checked = false; //一般
        document.forms[0].SUC_RADIO[1].checked = false; //追加繰上
        document.forms[0].SUC_RADIO[2].checked = false; //附属推薦
        document.forms[0].STARTNUMBER.value = document.forms[0].max_failure_noticeno.value

    }else if(obj.value == 11 || obj.value == 12 || obj.value == 13){   //合格者(一般,追加繰上,附属推薦)
        document.forms[0].OUTPUT[0].checked = true;     //合格者
        document.forms[0].OUTPUT[1].checked = false;    //不合格者
        document.forms[0].STARTNUMBER.value = document.forms[0].max_success_noticeno.value
    }

    document.forms[0].PASSCOUNT.disabled = (obj.value == 12)? false : true ;
}

