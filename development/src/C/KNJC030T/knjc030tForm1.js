function btn_submit(cmd)
{
    if (cmd == 'update') {

      var monthch = document.forms[0].monthch.value;
        if(monthch == '0'){
          alert('{rval MSG915}'+'許可月範囲外');
          return false;
        }

      var attendctrldate = document.forms[0].attendctrldate.value;
        if(attendctrldate == '0'){
          alert('{rval MSG915}'+'出欠制御日付範囲外');
          return false;
        }

      var attendexsits = document.forms[0].attendexsits.value;
        if(attendexsits == '1' && !confirm('{rval MSG105}')){
          return true;
        }


    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closing_window(no)
{
    alert('{rval MSG300}');
    closeWin();
    return true;
}

function celcolchan(src,ID)
{
    var lock = document.forms[0].locker.value;

   if(lock == 'off'){

        backup_obj = new Object();
        backup_obj = src;
        document.forms[0].ID_NO.value = src.id;
        src.bgColor = color_changer(src.bgColor);
        Style_Color(src.id);
        document.forms[0].locker.value = 'on';
        document.forms[0].backupper.value = src.id;

    }else if(lock == 'on'){
        var idno = document.forms[0].ID_NO.value;
        bk = new Object();
        bk = document.all(idno);

        if(src.id == bk.id)
        {
            src.bgColor = color_changer(src.bgColor);
            Style_Color(src.id);
            document.forms[0].locker.value = 'off';
            document.forms[0].ID_NO.value = '';
        }else{
             var thisis = document.forms[0].cmd.value;
             document.forms[0].locker.value = 'on';
             bk.bgColor = color_changer(bk.bgColor);
             src.bgColor = color_changer(src.bgColor);
             Style_Color(src.id);
             Style_Color(bk.id);
             document.forms[0].ID_NO.value = src.id;
             bk = src;
       }
    }

}

function color_changer(color)
{
    var color_blue    = '#3399ff';
    var color_red     = '#ff0099';
    var color_yellow  = '#ffff00';
    var blue_change   = '#99ccff';
    var red_change    = '#ffccff';
    var yellow_change = '#ffffcc';
    var color_black   = '#000000';
    var color_white   = '#ffffff';
    var return_color;

    switch(color){
        case color_blue :
            return_color = blue_change ;
            break;
        case color_red :
            return_color = red_change ;
            break;
        case color_yellow :
            return_color = yellow_change ;
            break;
        case blue_change :
            return_color = color_blue ;
            break;
        case red_change :
            return_color = color_red ;
            break;
        case yellow_change :
            return_color = color_yellow ;
            break;
        case color_black :
            return_color = color_white;
            break;
        case color_white :
            return_color = color_black;
            break;
        default :
            return_color = color_black;
            break;
    }
    return return_color ;
}

function IsUserOK_ToJump(URL, syoribi, period, grade, hrclass, tr_cd1, staffcd)
{

      var monthch = document.forms[0].monthch.value;
        if(monthch == '0'){
          alert('{rval MSG915}'+'許可月範囲外');
          return false;
        }

      var attendctrldate = document.forms[0].attendctrldate.value;
        if(attendctrldate == '0'){
          alert('{rval MSG915}'+'出欠制御日付範囲外');
          return false;
        }


//    window.open(URL+'?syoribi='+syoribi+'&periodcd='+period+'&grade='+grade+'&hrclass='+hrclass+'&tr_cd1='+tr_cd1+'&staffcd='+staffcd);
    wopen(URL+'?syoribi='+syoribi+'&periodcd='+period+'&grade='+grade+'&hrclass='+hrclass+'&tr_cd1='+tr_cd1+'&staffcd='+staffcd,'name',0,0,screen.availWidth,screen.availHeight);//2006/03/15 alp
}

function Style_Color(set_id)
{
    var color = "";
        color = document.all(set_id).style.color ;
        document.all(set_id).style.color = color_changer(color);
}
