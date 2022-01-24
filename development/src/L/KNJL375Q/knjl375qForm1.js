//jQuery
$(function(){
   $('input[name="ABSENCE"]').change(function(){
      
        var satNo = $(this).val();
        
        var staffcd = document.forms[0].staffcd.value;
        var kamoku = document.forms[0].kamoku.value;
        var ctrlYear = document.forms[0].ctrlYear.value;
        
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
                        "staffcd" : staffcd,
                        "ctrlYear" : ctrlYear,
                        "kamoku" : kamoku,
                         },
               }); 
        document.forms[0]['TOKUTEN'+satNo].value = '';
   });

});


window.onload = function(){
}

function btn_submit(cmd) {   


    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function closecheck() {
    parent.window.close();
}


function update_check(cmd, commentno, countno){
    
    if(cmd == 'delete'){
        if(!confirm('削除してよろしいですか？')){
            return false;
        }
    }
    
    document.forms[0].COMMENT_NO.value = commentno;
    document.forms[0].COUNT_NO.value = countno;
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;

}

//ENTER押したら次のテキストボックスへ
function toNextText(text)
{
    // Ent13 Tab9 ←37 ↑38 →39 ↓40
    var e = window.event;
    //方向キー
    //var moveEnt = e.keyCode;
    if (e.keyCode != 13) {
        return;
    }
    var textid = text.id;
    var id = textid.replace("TOKUTEN", "");
    var nextid = parseInt(id) + 1;
    var nextText = document.getElementById("TOKUTEN"+nextid);
    
    if(nextText == null){
        return;
    }else{
        nextText.focus();
        nextText.select();
        return;
    }
}

//onBlurで得点を1つずつ更新できるか
function updateTokuten(text)
{
    text.value = toInteger(text.value);
    
    if(text.value != ""){
        var textname = text.name;
        var satno = textname.replace("TOKUTEN", "");
        
        var ctrlYear = document.forms[0].ctrlYear.value;
        var staffcd = document.forms[0].staffcd.value;
        var kamoku = document.forms[0].kamoku.value;
        var kaisu = document.forms[0].kaisu.value;
        
        var tokuten = text.value;

        
        $.ajax({
                type : "POST",
                url : "updateTokuten.php",
                data: { "satno" : satno,
                        "tokuten" : tokuten,
                        "ctrlYear" : ctrlYear,
                        "staffcd" : staffcd,
                        "kamoku" : kamoku,
                        "kaisu" : kaisu,
                     },
               }); 
    }
}
//テキストボックスの隣に削除ボタン
function delBtn(satno)
{
        var ctrlYear = document.forms[0].ctrlYear.value;
        var staffcd = document.forms[0].staffcd.value;
        var kamoku = document.forms[0].kamoku.value;
        var kaisu = document.forms[0].kaisu.value;
        
        var tokuten = "null";

        
        $.ajax({
                type : "POST",
                url : "updateTokuten.php",
                data: { "satno" : satno,
                        "tokuten" : tokuten,
                        "ctrlYear" : ctrlYear,
                        "staffcd" : staffcd,
                        "kamoku" : kamoku,
                        "kaisu" : kaisu,
                     },
                success: function(){
                    var name = "TOKUTEN"+satno;
                    document.forms[0][name].value = "";
                }
           }); 
}
