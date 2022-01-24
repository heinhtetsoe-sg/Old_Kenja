/*
 * kanji=漢字
 * <?php

require_once('for_php7.php');
 # $Id: knjd500kForm1.js 56580 2017-10-22 12:35:29Z maeshiro $ ?>
 */

function closing_window(no)
{
    var msg;

    if(no == 'year'){
      msg = '{rval MSG305}';
    }else{
      msg = '{rval MSG300}';
    }

    alert(msg);
    closeWin();
    return true;
}

//--start
function btn_submit(cmd)
{
    if ((cmd == 'update' | cmd == 'cancel') && document.forms[0].txtCount.value == '0')
    {
        alert('{rval MSG304}');
        return false;
    }
    if(cmd == 'cancel' && !confirm('{rval MSG106}'))  return true;
    if(cmd == 'show_all'){
        document.forms[0].shw_flg.value = (document.forms[0].shw_flg.value == 'on')? 'off' : 'on';
        cmd = '';
    }else if(cmd == ''){
        document.forms[0].shw_flg.value = 'off';
    }

  document.forms[0].cmd.value = cmd;
  document.forms[0].submit();
    return false;
}

function setData()
{
    var i;
    var targetid = 0;

    if (document.forms[0].txtCount.value == '0')
    {
        alert('{rval MSG304}');
        return false;
    }

    for(i=0; i<document.forms[0].dataCount.value; i++){
        if(document.all["chk_box"+i] == undefined) continue;
        if(document.all["chk_box"+i].checked){
            targetid = document.all["chk_box"+i].id;
            var val  = document.all["chk_box"+i].value;
            document.getElementById("t" + targetid).value = val;
//            var val  = val.split('-');
//            if (val[1]!='' && document.getElementById("i" + targetid) != undefined) {
//                document.getElementById("i" + targetid).value = val[1];
//            }
//            if (val[0]!='' && document.getElementById("t" + targetid) != undefined) {
//                document.getElementById("t" + targetid).value = val[0];
//            }
//            if (val[2]!='' && document.getElementById("a" + targetid) != undefined) {
//                document.getElementById("a" + targetid).value = val[2];
//            }
        }
    }
}

function chk_Num(that){
    that.value = toInteger(that.value);
    if(that.value > 100){
        that.value = 100;
    }
}



//--end


function windowOpener(URL, staffcd, cntl_dt_key,tr,mode){
    if(!CheckOutPutCtrlDate(cntl_dt_key)){
        return false;
    }
    if(mode == 'multi'){
        URL = URL + '?datekey=' + cntl_dt_key + '&stfcd=' + staffcd + '&tr=' + tr + '&mode=off';;
        wopen(URL,'WhatIsThis',100,100,650,250);
    }else if(mode == 'multiA'){
        URL = URL + '?datekey=' + cntl_dt_key + '&stfcd=' + staffcd + '&tr=' + tr + '&mode=on';
        wopen(URL,'WhatIsThis',100,100,650,250);
    }else{
        window.open(URL+'?syoribi='+cntl_dt_key+'&periodcd=1&STAFFCD='+staffcd+'&chaircd=' + mode);
    }
        return;
}

function CheckOutPutCtrlDate(cntl_dt_key)
{
    var DateStr = document.forms[0].output_CtrlDate.value;
    var sec_chk = document.forms[0].Security.value;
    sec_chk = sec_chk.split(',');

    if (sec_chk[0] == 'update_restrict' && cntl_dt_key <= DateStr) { 
        alert('{rval MSG300}'+ '\r\n（ 出欠制御日付: ' + DateStr + '以前のデータ ）');
        return false;    
    }
    return true;
}

function Page_jumper(jump)
{
  window.location.replace(jump);
}

