function btn_submit(cmd) {
    
    var c1;
    var c2;
    var c3;
    if (cmd == 'clear'){
    if (!confirm('{rval MSG106}'))
            return false;
        else
            cmd = '';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function MakeOrder()
{
    var arr = new Array();
    var ii = 0;
    var attendno = 1;
    var flag = 0;
    var wk_string;

    for (var i=0;i<document.forms[0].elements.length;i++)
    {
        if (document.forms[0].elements[i].type=='text' && document.forms[0].elements[i].value!="") {
            for(y=0;y<arr.length;y++)
            {
                f = arr[y];
                if(document.forms[0].elements[i].value == f){
                    alert('{rval MSG302}');
                    return false;
                }
            }
            arr[ii] = document.forms[0].elements[i].value;
            ii++;
        }
    }
    for (var i=0;i<document.forms[0].elements.length;i++)
    {
        var e = document.forms[0].elements[i];
        if (e.type=='text'){
            if(e.value == ''){
                flag = 1;

                //e.value = attendno;  2004-06-03 Y.ARAKAKI
                wk_string = "000" + attendno
                e.value = wk_string.substr(wk_string.length - 3, 3);

                attendno++;
            }else if(e.value != ''){
                flag = 2;
                attendno = e.value;
                wk_string = "000" + attendno
                e.value = wk_string.substr(wk_string.length - 3, 3);

                attendno++;
            }
        }
        if(attendno > 999){
            alert("出席番号の最大値は999です");
            return false;
        }
        for(y=0;y<arr.length;y++)
        {
            f = arr[y];
            if(flag == 1 && e.value == f){
                alert('{rval MSG302}');
                return false;
            }
        }
    }
}

function OnErrorStat(flg)
{
    if (flg == 1) {
        alert('処理学期が最終学期のため実行できません。');
    }
    closeWin();
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function ClearAttendno() 
{
    for (var i=0;i<document.forms[0].elements.length;i++)
    {
        var e = document.forms[0].elements[i];
        if (e.type=='text'){
            e.value = "";
        }
    }
}


