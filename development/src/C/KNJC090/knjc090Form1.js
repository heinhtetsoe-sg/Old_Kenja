function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){

    if (document.forms[0].DATE1.value == "")
    {
        alert("日付が不正です。");
        document.forms[0].DATE1.focus();
        return;
    }
    if (document.forms[0].DATE2.value == "")
    {
        alert("日付が不正です。。");
        document.forms[0].DATE2.focus();
        return;
    }

    var t1 = document.forms[0].DATE1.value;
    var t2 = document.forms[0].DATE2.value;
    var temp1 = t1.split('/');
    var temp2 = t2.split('/');

    var wrkdate = "";

    if ( new Date(eval(temp1[0]),eval(temp1[1])-1,eval(temp1[2])) > new Date(eval(temp2[0]),eval(temp2[1])-1,eval(temp2[2])) )
    {
        wrkdate = t2;
        t2 = t1;
        t1 = wrkdate;
        document.forms[0].DATE1.value = t1;
        document.forms[0].DATE2.value = t2;
    }

    var temp1 = t1.split('/');
    var temp2 = t2.split('/');

    var val = document.forms[0].SEME_DATE.value;
    var tmp = val.split(',');
    var tmp1f = tmp[0].split('/'); //学期開始日付（1学期）
    var tmp1t = tmp[1].split('/'); //学期終了日付（1学期）
    var tmp2f = tmp[2].split('/'); //学期開始日付（2学期）
    var tmp2t = tmp[3].split('/'); //学期終了日付（2学期）
    var tmp3f = tmp[4].split('/'); //学期開始日付（3学期）
    var tmp3t = tmp[5].split('/'); //学期終了日付（3学期）

    //学期フラグ初期化
    var flag = 0;

    //学期コード取得
    var val_seme = "";
    val_seme = document.forms[0].SEMESTER.value;
    document.forms[0].SEMESTER.value = "";

    //１学期の場合（１学期開始日 ＜＝ 開始日 ＜＝ 終了日 ＜＝ １学期終了日）
    if(new Date(eval(tmp1f[0]),eval(tmp1f[1])-1,eval(tmp1f[2])) <= new Date(eval(temp1[0]),eval(temp1[1])-1,eval(temp1[2])))
    {
        if(new Date(eval(temp2[0]),eval(temp2[1])-1,eval(temp2[2])) <= new Date(eval(tmp1t[0]),eval(tmp1t[1])-1,eval(tmp1t[2])) )
        {
            flag = 1;       //1学期
        }
    }

    //２学期の場合（２学期開始日 ＜＝ 開始日 ＜＝ 終了日 ＜＝ ２学期終了日）
    if(new Date(eval(tmp2f[0]),eval(tmp2f[1])-1,eval(tmp2f[2])) <= new Date(eval(temp1[0]),eval(temp1[1])-1,eval(temp1[2])) )
    {
        if(new Date(eval(temp2[0]),eval(temp2[1])-1,eval(temp2[2])) <= new Date(eval(tmp2t[0]),eval(tmp2t[1])-1,eval(tmp2t[2])) )
        {
            flag = 2;       //2学期
        }
    }

    //３学期の場合（３学期開始日 ＜＝ 開始日 ＜＝ 終了日 ＜＝ ３学期終了日）
    if(new Date(eval(tmp3f[0]),eval(tmp3f[1])-1,eval(tmp3f[2])) <= new Date(eval(temp1[0]),eval(temp1[1])-1,eval(temp1[2])) )
    {
        if(new Date(eval(temp2[0]),eval(temp2[1])-1,eval(temp2[2])) <= new Date(eval(tmp3t[0]),eval(tmp3t[1])-1,eval(tmp3t[2])) )
        {
            flag = 3;       //3学期
        }
    }

    //学期期間に当てはまる場合、その学期を新しい学期に、当てはまらない場合リターン
    if ((flag >=1) && (flag<= 3))
    {
        document.forms[0].SEMESTER.value = flag;
    }
    else
    {
        alert("日付が学期範囲外です。");
        document.forms[0].DATE2.focus();
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function kubun()
{
    var kubun1 = document.forms[0].OUTPUT1;
    var kubun2 = document.forms[0].OUTPUT2;
    var kubun3 = document.forms[0].OUTPUT3;

    if( (kubun1.checked == false) && (kubun2.checked == false) && (kubun3.checked == false) )
    {
        flag3 = true;
    }
    else
    {
        flag3 = false;
    }
    document.forms[0].btn_print.disabled = flag3;
}

