function btn_submit(cmd) {

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG308}');
        return true;
    }

    if (cmd == 'update' && !confirm('{rval MSG102}')){
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function moji_hantei(that)
{
  kekka=0;
  moji=that.value;
  for(i=0; i<moji.length; i++)
      {
      dore=escape(moji.charAt(i));
      if(navigator.appName.indexOf("Netscape")!=-1)
          {
              if(dore.length>3 && dore.indexOf("%")!=-1){
              }
          }
      else 
          {
              if(dore.indexOf("%uFF")!=-1 && '0x'+dore.substring(2,dore.length) < 0xFF60){
                  kekka++;
              }else if(moji.match(/\W/g) != null && dore.length == 6){

                  kekka++;
              }

          }
      }
  if(kekka>0){
  alert("全角文字が含まれています。");
      srch='';

      that.value=moji.replace(/\W/g, srch);

  }

}
