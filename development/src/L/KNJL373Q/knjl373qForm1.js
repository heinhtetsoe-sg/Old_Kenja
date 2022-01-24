//jQuery
$(function(){
   $('input[name="ABSENCE"]').change(function(){
      
        var satNo = $(this).val();
        
        var staffcd = document.forms[0].staffcd.value;
        
        if($(this).prop('checked')){
            var Mode = "0";
        }else{
            var Mode = "1";
        }
        
        $.ajax({
                type : "POST",
                url : "ajax.php",
                data: { "satno" : satNo,
                        "mode" : Mode,
                        "staffcd" : staffcd },
               }); 
   });

});



function btn_submit(cmd) {
    
    if(cmd == "delete"){
        if(!confirm('削除してよろしいですか?')){
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

//セキュリティーチェック
function OnSecurityError()
{
    alert('{rval MSG300}' + '\n高セキュリティー設定がされています。');
    closeWin();
}
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

