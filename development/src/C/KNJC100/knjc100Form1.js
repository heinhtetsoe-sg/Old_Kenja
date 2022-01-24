/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function btn_submit(cmdb)           //読込ボタン
{

    if(document.forms[0].RADIO[0].checked == true)
    {
        if (document.forms[0].DATE1.value == "")
        {
            alert("日付が不正です。");
            document.forms[0].DATE1.focus();
            return;
        }
        if (document.forms[0].DATE2.value == "")
        {
            alert("日付が不正です。");
            document.forms[0].DATE2.focus();
            return;
        }
    }
    if(document.forms[0].RADIO[1].checked == true)
    {
        if (document.forms[0].DATE1.value == "")
        {
            alert("日付が不正です。");
            document.forms[0].DATE1.focus();
            return;
        }
    }

    //「科目別勤怠集計表」の時は、日付１、２両方チェック
    if(document.forms[0].RADIO[0].checked == true)  
    {
        var val = document.forms[0].SEME_DATE.value;
        var tmp = val.split(',');
        var tmp2 = document.forms[0].DATE1.value.split('/'); //印刷範囲開始日付
        var tmp3 = document.forms[0].DATE2.value.split('/'); //印刷範囲終了日付
        var tmp4 = tmp[0].split('/'); //学期開始日付（1学期）
        var tmp5 = tmp[1].split('/'); //学期終了日付（1学期）
        var tmp6 = tmp[2].split('/'); //学期開始日付（2学期）
        var tmp7 = tmp[3].split('/'); //学期終了日付（2学期）
        var tmp8 = tmp[4].split('/'); //学期開始日付（3学期）
        var tmp9 = tmp[5].split('/'); //学期終了日付（3学期）

        if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) > new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
        {
            alert("日付の大小が不正です。");
            return;
        }
        var flag1 = 0;
        var flag2 = 0;
        var val_seme = "";
        val_seme = document.forms[0].SEMESTER.value;
        if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2])))
            {
                flag1 = 1; //1学期
            }
        }
        if (new Date(eval(tmp6[0]),eval(tmp6[1])-1,eval(tmp6[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp7[0]),eval(tmp7[1])-1,eval(tmp7[2])))
            {
                flag1 = 2; //2学期
            }
        }
        if (new Date(eval(tmp8[0]),eval(tmp8[1])-1,eval(tmp8[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp9[0]),eval(tmp9[1])-1,eval(tmp9[2])))
            {
                flag1 = 3; //3学期
            }
        }
        if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
        {
            if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2])))
            {
                flag2 = 1; //1学期
            }
        }
        if (new Date(eval(tmp6[0]),eval(tmp6[1])-1,eval(tmp6[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
        {
            if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp7[0]),eval(tmp7[1])-1,eval(tmp7[2])))
            {
                flag2 = 2; //2学期
            }
        }
        if (new Date(eval(tmp8[0]),eval(tmp8[1])-1,eval(tmp8[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
        {
            if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp9[0]),eval(tmp9[1])-1,eval(tmp9[2])))
            {
                flag2 = 3; //3学期
            }
        }
        if (flag1 != flag2)
        {
            alert("指定範囲が学期をまたがっています。");
            return;
        }
        if ( (flag1 == "") || (flag2 == "") )
        {
            alert("指定範囲が学期外です。");
            return;
        }
        if(val_seme != flag1)
        {
            document.forms[0].CLASS_SELECTED.length = 0;    //学期が変わると対象クラスをクリア
            document.forms[0].cmd.value = "semechg";
            document.forms[0].submit();
            return;         //学期が変わった場合（日付の変更後すぐにクリックされたとき）は何もしない
        }

    }
    else    //「本日の勤怠状況」の時は、日付１のみチェック
    {
        var val = document.forms[0].SEME_DATE.value;
        var tmp = val.split(',');
        var tmp2 = document.forms[0].DATE1.value.split('/'); //印刷範囲開始日付
        var tmp4 = tmp[0].split('/'); //学期開始日付（1学期）
        var tmp5 = tmp[1].split('/'); //学期終了日付（1学期）
        var tmp6 = tmp[2].split('/'); //学期開始日付（2学期）
        var tmp7 = tmp[3].split('/'); //学期終了日付（2学期）
        var tmp8 = tmp[4].split('/'); //学期開始日付（3学期）
        var tmp9 = tmp[5].split('/'); //学期終了日付（3学期）
        var flag1 = 0;
        var val_seme = "";
        val_seme = document.forms[0].SEMESTER.value;
        if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2])))
            {
                flag1 = 1; //1学期
            }
        }
        if (new Date(eval(tmp6[0]),eval(tmp6[1])-1,eval(tmp6[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp7[0]),eval(tmp7[1])-1,eval(tmp7[2])))
            {
                flag1 = 2; //2学期
            }
        }
        if (new Date(eval(tmp8[0]),eval(tmp8[1])-1,eval(tmp8[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp9[0]),eval(tmp9[1])-1,eval(tmp9[2])))
            {
                flag1 = 3; //3学期
            }
        }
        if (flag1 == "")
        {
            alert("日付が学期の範囲外です");
            return;
        }
        if(val_seme != flag1)
        {
            document.forms[0].CLASS_SELECTED.length = 0;    //学期が変わると対象クラスをクリア
            document.forms[0].cmd.value = "semechg";
            document.forms[0].submit();
            return;         //学期が変わった場合（日付の変更後すぐにクリックされたとき）は何もしない
        }

    }

    document.forms[0].cmd.value = cmdb;
    document.forms[0].submit();
    document.forms[0].DATE1.focus();        // add 02/10/02
    return false;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function date_submit(cmdd)          //日付の値が変わったとき
{
    document.forms[0].CLASS_NAME.length = 0;
    document.forms[0].CLASS_SELECTED.length = 0;

    //「科目別勤怠集計表」の時は、日付１、２両方チェック
    if(document.forms[0].RADIO[0].checked == true)  
    {
        var val = document.forms[0].SEME_DATE.value;
        var tmp = val.split(',');
        var tmp2 = document.forms[0].DATE1.value.split('/'); //印刷範囲開始日付
        var tmp3 = document.forms[0].DATE2.value.split('/'); //印刷範囲終了日付
        var tmp4 = tmp[0].split('/'); //学期開始日付（1学期）
        var tmp5 = tmp[1].split('/'); //学期終了日付（1学期）
        var tmp6 = tmp[2].split('/'); //学期開始日付（2学期）
        var tmp7 = tmp[3].split('/'); //学期終了日付（2学期）
        var tmp8 = tmp[4].split('/'); //学期開始日付（3学期）
        var tmp9 = tmp[5].split('/'); //学期終了日付（3学期）

        if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) > new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
        {
            return;         //DATE1＞DATE2の場合何もしない
        }

        var flag1 = 0;
        var flag2 = 0;
        var val_seme = "";
        val_seme = document.forms[0].SEMESTER.value;

        if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2])))
            {
                flag1 = 1; //1学期
            }
        }
        if (new Date(eval(tmp6[0]),eval(tmp6[1])-1,eval(tmp6[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp7[0]),eval(tmp7[1])-1,eval(tmp7[2])))
            {
                flag1 = 2; //2学期
            }
        }
        if (new Date(eval(tmp8[0]),eval(tmp8[1])-1,eval(tmp8[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp9[0]),eval(tmp9[1])-1,eval(tmp9[2])))
            {
                flag1 = 3; //3学期
            }
        }
        if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
        {
            if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2])))
            {
                flag2 = 1; //1学期
            }
        }
        if (new Date(eval(tmp6[0]),eval(tmp6[1])-1,eval(tmp6[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
        {
            if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp7[0]),eval(tmp7[1])-1,eval(tmp7[2])))
            {
                flag2 = 2; //2学期
            }
        }
        if (new Date(eval(tmp8[0]),eval(tmp8[1])-1,eval(tmp8[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
        {
            if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp9[0]),eval(tmp9[1])-1,eval(tmp9[2])))
            {
                flag2 = 3; //3学期
            }
        }
        if (flag1 != flag2)
        {
            return;     //学期が範囲をまたがっている場合なにもしない。
        }
        if ( (flag1 == "") || (flag2 == "") )
        {
            return;     //学期範囲外の場合何もしない。
        }
        if (val_seme == flag1)
        {
            return;     //学期が変わらない場合何もしない。
        }
        document.forms[0].cmd.value = cmdd;
        document.forms[0].submit();
        document.forms[0].DATE1.focus();        // add 02/10/02
        return false;
    }
    else    //「本日の勤怠状況」の時は、日付１のみチェック
    {
        var val = document.forms[0].SEME_DATE.value;
        var tmp = val.split(',');
        var tmp2 = document.forms[0].DATE1.value.split('/'); //印刷範囲開始日付
        var tmp4 = tmp[0].split('/'); //学期開始日付（1学期）
        var tmp5 = tmp[1].split('/'); //学期終了日付（1学期）
        var tmp6 = tmp[2].split('/'); //学期開始日付（2学期）
        var tmp7 = tmp[3].split('/'); //学期終了日付（2学期）
        var tmp8 = tmp[4].split('/'); //学期開始日付（3学期）
        var tmp9 = tmp[5].split('/'); //学期終了日付（3学期）
        var flag1 = 0;
        var val_seme = "";
        val_seme = document.forms[0].SEMESTER.value;
        if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2])))
            {
                flag1 = 1; //1学期
            }
        }
        if (new Date(eval(tmp6[0]),eval(tmp6[1])-1,eval(tmp6[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp7[0]),eval(tmp7[1])-1,eval(tmp7[2])))
            {
                flag1 = 2; //2学期
            }
        }
        if (new Date(eval(tmp8[0]),eval(tmp8[1])-1,eval(tmp8[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp9[0]),eval(tmp9[1])-1,eval(tmp9[2])))
            {
                flag1 = 3; //3学期
            }
        }
        if (flag1 == "")
        {
            return;         //学期範囲外の場合何もしない
        }
        if (val_seme == flag1)
        {
            return;         //学期が変わらない場合何もしない。
        }

        document.forms[0].cmd.value = cmdd;
        document.forms[0].submit();
        document.forms[0].DATE1.focus();        // add 02/10/02
        return false;

    }

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//印刷・プレビューボタン
function newwin(SERVLET_URL){

    if(document.forms[0].RADIO[0].checked == true)
    {
        if (document.forms[0].DATE1.value == "")
        {
            alert("日付が不正です。");
            document.forms[0].DATE1.focus();
            return;
        }
        if (document.forms[0].DATE2.value == "")
        {
            alert("日付が不正です。");
            document.forms[0].DATE2.focus();
            return;
        }
    }
    if(document.forms[0].RADIO[1].checked == true)
    {
        if (document.forms[0].DATE1.value == "")
        {
            alert("日付が不正です。");
            document.forms[0].DATE1.focus();
            return;
        }
    }

    //「科目別勤怠集計表」の時は、日付１、２両方チェック
    if(document.forms[0].RADIO[0].checked == true)  
    {
        var val = document.forms[0].SEME_DATE.value;
        var tmp = val.split(',');
        var tmp2 = document.forms[0].DATE1.value.split('/'); //印刷範囲開始日付
        var tmp3 = document.forms[0].DATE2.value.split('/'); //印刷範囲終了日付
        var tmp4 = tmp[0].split('/'); //学期開始日付（1学期）
        var tmp5 = tmp[1].split('/'); //学期終了日付（1学期）
        var tmp6 = tmp[2].split('/'); //学期開始日付（2学期）
        var tmp7 = tmp[3].split('/'); //学期終了日付（2学期）
        var tmp8 = tmp[4].split('/'); //学期開始日付（3学期）
        var tmp9 = tmp[5].split('/'); //学期終了日付（3学期）

        if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) > new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
        {
            alert("日付の大小が不正です。");
            return;
        }
        var flag1 = 0;
        var flag2 = 0;
        var val_seme = "";
        val_seme = document.forms[0].SEMESTER.value;
        if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2])))
            {
                flag1 = 1; //1学期
            }
        }
        if (new Date(eval(tmp6[0]),eval(tmp6[1])-1,eval(tmp6[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp7[0]),eval(tmp7[1])-1,eval(tmp7[2])))
            {
                flag1 = 2; //2学期
            }
        }
        if (new Date(eval(tmp8[0]),eval(tmp8[1])-1,eval(tmp8[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp9[0]),eval(tmp9[1])-1,eval(tmp9[2])))
            {
                flag1 = 3; //3学期
            }
        }
        if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
        {
            if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2])))
            {
                flag2 = 1; //1学期
            }
        }
        if (new Date(eval(tmp6[0]),eval(tmp6[1])-1,eval(tmp6[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
        {
            if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp7[0]),eval(tmp7[1])-1,eval(tmp7[2])))
            {
                flag2 = 2; //2学期
            }
        }
        if (new Date(eval(tmp8[0]),eval(tmp8[1])-1,eval(tmp8[2])) <= new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])))
        {
            if(new Date(eval(tmp3[0]),eval(tmp3[1])-1,eval(tmp3[2])) <= new Date(eval(tmp9[0]),eval(tmp9[1])-1,eval(tmp9[2])))
            {
                flag2 = 3; //3学期
            }
        }
        if (flag1 != flag2)
        {
            alert("指定範囲が学期をまたがっています。");
            return;
        }
        if ( (flag1 == "") || (flag2 == "") )
        {
            alert("指定範囲が学期外です。");
            return;
        }
        if(val_seme != flag1)
        {
            document.forms[0].CLASS_SELECTED.length = 0;    //学期が変わると対象クラスをクリア
            document.forms[0].cmd.value = "semechg";
            document.forms[0].submit();
            return;         //学期が変わった場合（日付の変更後すぐにクリックされたとき）は何もしない
        }
    }
    else    //「本日の勤怠状況」の時は、日付１のみチェック
    {
        var val = document.forms[0].SEME_DATE.value;
        var tmp = val.split(',');
        var tmp2 = document.forms[0].DATE1.value.split('/'); //印刷範囲開始日付
        var tmp4 = tmp[0].split('/'); //学期開始日付（1学期）
        var tmp5 = tmp[1].split('/'); //学期終了日付（1学期）
        var tmp6 = tmp[2].split('/'); //学期開始日付（2学期）
        var tmp7 = tmp[3].split('/'); //学期終了日付（2学期）
        var tmp8 = tmp[4].split('/'); //学期開始日付（3学期）
        var tmp9 = tmp[5].split('/'); //学期終了日付（3学期）
        var flag1 = 0;
        var val_seme = "";
        val_seme = document.forms[0].SEMESTER.value;
        if(new Date(eval(tmp4[0]),eval(tmp4[1])-1,eval(tmp4[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp5[0]),eval(tmp5[1])-1,eval(tmp5[2])))
            {
                flag1 = 1; //1学期
            }
        }
        if (new Date(eval(tmp6[0]),eval(tmp6[1])-1,eval(tmp6[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp7[0]),eval(tmp7[1])-1,eval(tmp7[2])))
            {
                flag1 = 2; //2学期
            }
        }
        if (new Date(eval(tmp8[0]),eval(tmp8[1])-1,eval(tmp8[2])) <= new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])))
        {
            if(new Date(eval(tmp2[0]),eval(tmp2[1])-1,eval(tmp2[2])) <= new Date(eval(tmp9[0]),eval(tmp9[1])-1,eval(tmp9[2])))
            {
                flag1 = 3; //3学期
            }
        }
        if (flag1 == "")
        {
            alert("日付が学期の範囲外です");
            return;
        }
        if(val_seme != flag1)
        {
            document.forms[0].CLASS_SELECTED.length = 0;    //学期が変わると対象クラスをクリア
            document.forms[0].cmd.value = "semechg";
            document.forms[0].submit();
            return;         //学期が変わった場合（日付の変更後すぐにクリックされたとき）は何もしない
        }

    }

    if (document.forms[0].CLASS_SELECTED.length == 0)
    {
        alert('出力対象クラスを指定してください');
        return;
    }


    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++)
    {  
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++)
    {  
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();
    document.forms[0].action = action;
    document.forms[0].target = target;
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

function kintai(num)
{
    if(num.value == 1)
    {
//      document.forms[0].DATE1.disabled  = false;
//      document.forms[0].btn_calen1.disabled  = false;
        document.forms[0].DATE2.disabled  = false;
//      document.forms[0].btn_calen2.disabled  = false;
        var j=0;
        for (var i=0;i<document.forms[0].elements.length;i++)
        {
            var e = document.forms[0].elements[i];
            if (e.type=='button' && e.name=='btn_calen'){
                if(j==1) e.disabled = false;
                j++;
            }
        }
    }
    else
    {
//      document.forms[0].DATE1.disabled  = false;
//      document.forms[0].btn_calen1.disabled  = false;
        document.forms[0].DATE2.disabled  = true;
//      document.forms[0].btn_calen2.disabled  = true;
        var j=0;
        for (var i=0;i<document.forms[0].elements.length;i++)
        {
            var e = document.forms[0].elements[i];
            if (e.type=='button' && e.name=='btn_calen'){
                if(j==1) e.disabled = true;
                j++;
            }
        }
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
    
function AllClearList(OptionList, TitleName) 
{
        attribute = document.forms[0].category_name;
        ClearList(attribute,attribute);
        attribute = document.forms[0].category_selected;
        ClearList(attribute,attribute);
}

function move1(side)
{   
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();   // 2004/01/26
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;
    
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left")
    {  
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    }
    else
    {  
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++)
    {  
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y; // 2004/01/26
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++)
    {   
        if ( attribute1.options[i].selected )
        {  
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            tempaa[y] = attribute1.options[i].value+","+y; // 2004/01/26
        }
        else
        {  
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();  // 2004/01/26

    //generating new options // 2004/01/26
    for (var i = 0; i < temp1.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
//      attribute2.options[i] = new Option();
//      attribute2.options[i].value = temp1[i];
//      attribute2.options[i].text =  tempa[i];
    }

    //generating new options
    ClearList(attribute1,attribute1);
    if (temp2.length>0)
    {   
        for (var i = 0; i < temp2.length; i++)
        {   
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }
}

function moves(sides)
{   
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();   // 2004/01/26
    var current5 = 0;
    var z=0;
    
    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left")
    {  
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = document.forms[0].CLASS_SELECTED;
    }
    else
    {  
        attribute5 = document.forms[0].CLASS_SELECTED;
        attribute6 = document.forms[0].CLASS_NAME;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++)
    {  
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z; // 2004/01/26
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++)
    {   
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = attribute5.options[i].value+","+z; // 2004/01/26
    }

    tempaa.sort();  // 2004/01/26

    //generating new options // 2004/01/26
    for (var i = 0; i < temp5.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
//      attribute6.options[i] = new Option();
//      attribute6.options[i].value = temp5[i];
//      attribute6.options[i].text =  tempc[i];
    }

    //generating new options
    ClearList(attribute5,attribute5);

}

function dis_date(flag)
{
    document.forms[0].DATE2.disabled = flag;
        var j=0;
        for (var i=0;i<document.forms[0].elements.length;i++)
        {
            var e = document.forms[0].elements[i];
            if (e.type=='button' && e.name=='btn_calen'){
                if(j==1) e.disabled = flag;
                j++;
            }
        }
}
