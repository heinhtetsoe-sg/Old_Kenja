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
    if (blank_flg == 2) {
        alert('{rval MZ0026}' + '�������ܤ���ꤷ�Ƥ���������');
        return;
    }
	len1 = jstrlen(document.forms[0].COMPANY_NAME.value);
	len2 = jstrlen(document.forms[0].SHUSHOKU_ADD.value);
    if ((len1 > 80) || (len2 > 80)) {
        alert('{rval MZ0044}' + '(̾�δ�����80�Х��ȤޤǤǤ�)');
        return;
    }
    top.opener.document.forms[0].COMPANY_NAME.value = document.forms[0].COMPANY_NAME.value;
    top.opener.document.forms[0].SHUSHOKU_ADD.value = document.forms[0].SHUSHOKU_ADD.value;
    top.opener.document.forms[0].cmd.value = 'search';
    top.opener.document.forms[0].submit();
    top.window.close();
    return false;
}

function jstrlen(str,   len, i) {
   len = 0;
   str = escape(str);
   for (i = 0; i < str.length; i++, len++) {
      if (str.charAt(i) == "%") {
         if (str.charAt(++i) == "u") {
            i += 3;
            len++;
         }
         i++;
      }
   }
   return len;
}

function SearchResult(){
    alert('{rval MZ0001}');
}
function OnAuthError()
{
    alert('{rval MZ0026}');
    closeWin();
}

function GoWin(company_cd){
    top.opener.document.edit.COMPANY_CD.value = company_cd;
    top.opener.document.forms[0].cmd.value = 'search';
    top.opener.document.forms[0].submit();
    top.window.close();
    return false;
}
