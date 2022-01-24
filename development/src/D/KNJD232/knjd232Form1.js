function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){

    if (!document.forms[0].OUTPUT1.checked &&
        !document.forms[0].OUTPUT2.checked &&
        !document.forms[0].OUTPUT3.checked &&
        !document.forms[0].OUTPUT4.checked &&
        !document.forms[0].OUTPUT5.checked &&
        !document.forms[0].OUTPUT6.checked)
    {
        alert('出力項目を指定してください。');
        return false;
    }
    if (document.forms[0].OUTPUT2.checked && (!document.forms[0].KYOUKA_SOUGOU1.checked && !document.forms[0].KYOUKA_SOUGOU2.checked)) {
        alert('「教科・科目」または「総合的な時間」どちらかを選択して下さい。');
        return false;
    }

    if (document.forms[0].OUTPUT1.checked && document.forms[0].ASSESS1.value == ""){
        alert('評定平均を指定してください。');
        return false;
    }
    if (document.forms[0].OUTPUT2.checked && document.forms[0].ASSESS2.value == ""){
        alert('評定を指定してください。');
        return false;
    }
    if (document.forms[0].OUTPUT2.checked && (document.forms[0].COUNT2.value == "" || document.forms[0].UNSTUDY2.value == "")){
        alert('科目数を指定してください。');
        return false;
    }
    if ((document.forms[0].OUTPUT2.checked ||
         document.forms[0].OUTPUT3.checked ||
         document.forms[0].OUTPUT4.checked ||
         document.forms[0].OUTPUT5.checked ||
         document.forms[0].OUTPUT6.checked ) && document.forms[0].DATE.value == "")
    {
        alert('日付を指定してください。');
        return false;
    }
    if (document.forms[0].DATE.value < document.forms[0].SDATE.value || document.forms[0].DATE.value > document.forms[0].EDATE.value) {
        alert("日付が学期範囲外です。");
        return;
    }
    document.forms[0].DATE.value = document.forms[0].DATE.value.replace("/","-");
    document.forms[0].DATE.value = document.forms[0].DATE.value.replace("/","-");
    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();
    document.forms[0].DATE.value = document.forms[0].DATE.value.replace("-","/");
    document.forms[0].DATE.value = document.forms[0].DATE.value.replace("-","/");

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function OverCheck(over)
{
    over.value = toFloat(over.value);
    if(document.forms[0].GAKKI.value == "9")
    {
        if(over.value > 5)
        {
            alert('学年末の評定平均（以上）が、"5"を超えています。');
            over.value = 5;
            over.focus();
            return false;
        }
        else if(over.value < 0)
        {
            alert('学年末の評定平均（以上）が、エラーです。');
            over.value = 0;
            over.focus();
            return false;
        }
    }
    else
    {
        if(over.value > 100)
        {
            alert('学期末の評定平均（以上）が、"100"を超えています。');
            over.value = 100;
            over.focus();
            return false;
        }
        else if(over.value < 0)
        {
            alert('学年末の評定平均（以上）が、エラーです。');
            over.value = 0;
            over.focus();
            return false;
        }
    }
}

function UnderCheck(under)
{
    if(document.forms[0].GAKKI.value == "9")
    {
        if(under.value > 5)
        {
            alert('学年末の評定平均（未満）が、"5"を超えています。');
            under.value = 5;
            under.focus();
            return false;
        }
        else if(under.value < 0)
        {
            alert('学年末の評定平均（未満）が、エラーです。');
            under.value = 0;
            under.focus();
            return false;
        }
    }
    else
    {
        if(under.value > 100)
        {
            alert('学期末の評定平均（未満）が、"100"を超えています。');
            under.value = 100;
            under.focus();
            return false;
        }
        else if(under.value < 0)
        {
            alert('学年末の評定平均（未満）が、エラーです。');
            under.value = 0;
            under.focus();
            return false;
        }
    }
}

function Default(def)
{
    if(def.value == "9")
    {
        document.forms[0].ASSESS1.value = 4.3;
        document.forms[0].YURYO_UNDER.value = 1;
    }
    else
    {
        document.forms[0].ASSESS1.value = 80;
        document.forms[0].YURYO_UNDER.value = 35;
    }
}


function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
