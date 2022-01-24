window.onload = function(){
}

function btn_submit(cmd) {   

    if(cmd == 'update'){
        var cnt = document.forms[0].dataCnt.value;
        cnt = Number(cnt);
        var errorFlg = 0;
        var errorPlace = 0;
        for(i=1;i<=cnt;i++){
            if((document.forms[0]["MIN"+i].value == "" && document.forms[0]["MAX"+i].value != "")
               || (document.forms[0]["MIN"+i].value != "" && document.forms[0]["MAX"+i].value == "")){
                    var errorFlg = 1;
                    var errorPlace = i;
                    break;
               }else if(document.forms[0]["MIN"+i].value == "" && document.forms[0]["MAX"+i].value == ""){
                    var errorFlg = 2;
                    break;
               }
            
        }
        if(errorFlg == 1){
            alert('点数は上限・下限両方を入力してください。');
            document.forms[0]["MIN"+errorPlace].focus();
            return false;
        }else if(errorFlg == 2){
            if(!confirm('点数が空のものは削除されますがよろしいですか？')){
                return false;
            }
        }
    }else if (cmd == 'copy'){
        if(document.forms[0].COPY_YEAR.value == ''){
            alert('コピーする年度を選択してください。');
            return false;
        }else{
            if(!confirm('現年度の作成済みデータはすべて削除されますが、コピーしてよろしいですか？')){
                return false;
            }
        }
    }
    
    
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
