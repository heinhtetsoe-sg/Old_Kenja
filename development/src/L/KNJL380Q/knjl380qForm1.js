window.onload = function(){
}

function btn_submit(cmd) {   

    if (cmd == 'copy'){
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
