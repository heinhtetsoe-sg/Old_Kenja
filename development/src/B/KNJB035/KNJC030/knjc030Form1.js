function btn_submit(cmd) {
  document.forms[0].cmd.value = cmd;
  document.forms[0].submit();
    return false;
}

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

