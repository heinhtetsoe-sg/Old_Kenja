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

function search_submit(){

    var blank_flg = 0;
    for (var i=0;i<document.forms[0].elements.length;i++)
    {
        var e = document.forms[0].elements[i];
        if (e.type != 'text' && e.type != 'select-one')
            continue;

        if (e.value != '') blank_flg++;
    }
    if (blank_flg == 0) {
        alert('{rval MSG300}' + '最低一項目を指定してください。');
        return;
    }
    top.opener.document.forms[0].SEARCH_CODE.value = document.forms[0].SEARCH_CODE.value;
    top.opener.document.forms[0].SEARCH_NAME.value = document.forms[0].SEARCH_NAME.value;
    top.opener.document.forms[0].SEARCH_KANA.value = document.forms[0].SEARCH_KANA.value;
    top.opener.document.forms[0].cmd.value = 'search';
    top.opener.document.forms[0].submit();
    top.window.close();
    return false;
}

