function btn_submit(cmd) {

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
            return false;
    }
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}'))
        return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function moji_hantei(that)
{
    kekka=0;
    moji=that.value;
    for(i=0; i<moji.length; i++)
    {
        dore=escape(moji.charAt(i));
        if((navigator.appName.indexOf("Netscape")!=-1) && dore.length==3 && dore.indexOf("%")!=-1)
            kekka++;
        else if((navigator.appName.indexOf("Internet Explorer")!=-1) && dore.length==6 && dore.indexOf("%uFF")!=-1 && '0x'+dore.substring(2,dore.length) > 0xFF66 && '0x'+dore.substring(2,dore.length) < 0xFFA0)
            kekka++;
    }
    if(kekka>0)alert("半角カナ文字が含まれています。");
//    srch='';
//    that.value=moji.replace(/\W/g, srch);
    }