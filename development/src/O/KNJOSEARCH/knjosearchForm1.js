function is_opener() {

    var ua = navigator.userAgent
    if(!!window.opener)
        if( ua.indexOf('MSIE 4')!=-1 && ua.indexOf('Win')!=-1)
             return !window.opener.closed
        else return typeof window.opener.document  == 'object'
    else return false

}
function observeDisp(){
  if (!is_opener()){
      top.window.close();
  }
}
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function search_submit(){

    var blank_flg = 0;
    for (var i=0;i<document.forms[0].elements.length;i++)
    {
        var e = document.forms[0].elements[i];
        if (e.type != 'text' && e.type != 'select-one')
            continue;

        if (e.value == '') blank_flg++;
    }
    if (blank_flg == 3) {
        alert('この処理は許可されていません。' + '最低一項目を指定してください。');
        return;
    }
    top.opener.document.forms[0].GRADE_HR_CLASS.value = document.forms[0].GRADE_HR_CLASS.value;
    top.opener.document.forms[0].KANJI.value = document.forms[0].KANJI.value;
    top.opener.document.forms[0].KANA.value = document.forms[0].KANA.value;
    top.opener.document.forms[0].cmd.value = 'search';
    top.opener.document.forms[0].submit();
    top.window.close();
    return false;
}

function SearchResult(){
    alert('データは存在していません。');
}
function OnAuthError()
{
    alert('この処理は許可されていません。');
    closeWin();
}

function GoWin(schregno){
    top.opener.document.forms[0].SCHREGNO.value = schregno;
    top.opener.document.forms[0].cmd.value = 'search';
    top.opener.document.forms[0].submit();
    top.window.close();
    return false;
}
