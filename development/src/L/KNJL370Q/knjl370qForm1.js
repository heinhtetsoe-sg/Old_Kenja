function btn_submit(cmd) {
    
    if (cmd == 'search'){
        var flg = 0;
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var el = document.forms[0].elements[i];
            if (el.name == "EXAMNO" || 
                el.name == "NAME_SEI" || 
                el.name == "NAME_MEI" || 
                el.name == "PLACECD" || 
                el.name == "SCHOOLCD" || 
                el.name == "GROUPCD") {
                
                
                
                if(el.value == ''){
                    flg++;
                }
            }
        }
        if(flg == 6){
            //何も入れなかったら全部表示したい
            //alert('検索項目を入力してください');
            //return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}


