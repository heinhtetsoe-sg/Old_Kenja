function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

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
    if (document.forms[0].GRADUATE_YEAR.value == "") {
        alert('卒業年度を指定して下さい。');
        return;
    }

    top.opener.document.forms[0].GRADUATE_YEAR.value = document.forms[0].GRADUATE_YEAR.value;
    top.opener.document.forms[0].GRADUATE_CLASS.value = document.forms[0].GRADUATE_CLASS.value;
    top.opener.document.forms[0].LKANJI.value = document.forms[0].LKANJI.value;
    top.opener.document.forms[0].LKANA.value = document.forms[0].LKANA.value;
    top.opener.document.forms[0].cmd.value = 'search';
    top.opener.document.forms[0].submit();
    top.window.close();
    return false;
}

