function btn_submit(cmd) {
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }

    if (cmd == "reset") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
    }
    //更新中の画面ロック(全フレーム)
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update' || cmd == 'delete') {
            updateFrameLocks();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function add() {

    var attribute;
    
    attribute = document.forms[0].SUBCLASS;
    
    for (var i = 0; i < attribute.length; i++)
    {     
        if ( attribute.options[i].selected )
        {  
            temp       = attribute.options[i].text.split(":");
            temp_value = attribute.options[i].value.split(":");    
        }
    }
   
    var subclasscd = (temp[0].split("-").length == 4) ? temp[0].split("-")[3] : temp[0];
    document.forms[0].SUBCLASSCD.value = subclasscd.substring(2, 6);
    document.forms[0].SUBCLASSNAME.value = temp[1];
    document.forms[0].SUBCLASSABBV.value = temp_value[1];
    document.forms[0].SUBCLASSNAME_ENG.value = temp_value[2];
    document.forms[0].SUBCLASSABBV_ENG.value = temp_value[3];
      
}

function check(that) {
    //全角から半角
    that.value = toHankakuNum(that.value);
    //数値型へ変換
    that.value = toInteger(that.value);
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
