/*
 * kanji=漢字
 * <?php

require_once('for_php7.php');
 # $Id: knjd510kForm1.js 56580 2017-10-22 12:35:29Z maeshiro $ ?>
 */
function closing_window(no)
{
    var msg;

    if(no == 'rp'){
        msg = '{rval MSG302}';
    }else if(no == 'cm'){
        msg = '{rval MSG300}';
    }
    alert(msg);
    closeWin();

    return true;
}

function btn_submit(cmd)
{
    if ((cmd == 'update' | cmd == 'cancel') && document.forms[0].dataCount.value == '0') {
        alert('{rval MSG304}');
        return false;
    }

    if(cmd == 'cancel' && !confirm('{rval MSG106}'))  return true;
    if(cmd == 'show_all')
    {
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
    var ii;
    var targetCell = "";
    var target;
    var str = Array(10);
    var tmp_ary = Array(10);
    var tmp_data;
    var cul = "";
    var serch_str;

    if (document.forms[0].dataCount.value == '0') {
        alert('{rval MSG304}');
        return false;
    }
    
    target = new  Object();
    target = document.all["tbl"];

    for(i=0; i<document.forms[0].dataCount.value; i++)
    {
        if(target.rows(i) == undefined) continue;

        tmp_data = target.rows(i).cells(0).innerHTML;
        tmp_data = "" + (tmp_data.match(/checked/i));

        if(tmp_data.match(/checked/i))
        {
            for(ii=4; ii<11; ii++)
            {
                str[ii] = target.rows(i).cells(ii).innerHTML;
                if(str[ii].match(/input/i))
                {
                    tmp_data = "" + (str[ii].match(/value=[0-9]*/i));
                    tmp_ary[ii] = (tmp_data.substring(6,tmp_data.length));

/*                    if(ii=="6" || ii =="9")
                    {
                        if(tmp_ary[ii-2] != "" && tmp_ary[ii-1] != ""){
                            cul = (eval(tmp_ary[ii-2])+eval(tmp_ary[ii-1]))/2;
                        }else if(tmp_ary[ii-2] != "" || tmp_ary[ii-1] != ""){
                            cul = (tmp_ary[ii-1] != "")? tmp_ary[ii-1] : tmp_ary[ii-2];
                        }
                    }
*/
                    if(ii=="6" || ii =="9")
                    {
                        if(tmp_ary[ii-1] != ""){
                            if(tmp_ary[ii-2] != ""){
                                cul = (eval(tmp_ary[ii-2])+eval(tmp_ary[ii-1]))/2;
                            } else if (str[ii].match(/class=testOn/i)) {
                                targetCell = "" + (str[ii].match(/id=a[0-9]*e/i));
                                targetCell = "" + (targetCell.substring(3,targetCell.length));
                                document.all[targetCell].value = "";
                            }else{
                                cul = tmp_ary[ii-1];
                            }
                        }else{
                            targetCell = "" + (str[ii].match(/id=a[0-9]*e/i));
                            targetCell = "" + (targetCell.substring(3,targetCell.length));
                            document.all[targetCell].value = "";
                       }
                    }

                    if(cul != "")
                    {
                        targetCell = "" + (str[ii].match(/id=a[0-9]*e/i));
                        targetCell = "" + (targetCell.substring(3,targetCell.length));
                        document.all[targetCell].value = Math.round(cul);
                    }

                    targetCell = "";
                    cul = "";

                }else{
                    tmp_ary[ii] = target.rows(i).cells(ii).innerHTML;
                }
            }
        }
    }
}

function chk_Num(that)
{
    that.value = toInteger(that.value);
    if(that.value > 100) that.value = 100;
}
