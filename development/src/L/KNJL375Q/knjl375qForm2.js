window.onload = function(){

}

function btn_submit(cmd) {   

    if(cmd == 'import'){
        var checkFlg = 0;
        for(i=1;i<=document.forms[0].R_KAMOKU.length;i++){
            if(document.getElementById('R_KAMOKU'+i).checked == true){
                checkFlg = 1;
            }
        }
        if(checkFlg != 1){
            alert('取り込むデータの科目を選択してください。');
            return false;
        }else if(document.forms[0].FILE.value == ""){
            alert('取込ファイルを選択してください。');
            return false;
        }
        if(!confirm('すでにデータが取り込まれていた場合、データを上書きしますがよろしいですか？')){
            return false;
        }
    }
    
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function closecheck() {
    parent.window.close();
}

function bottom_load(para) {
    
    parent.bottom_frame.location.href='knjl375qindex.php?cmd=edit&'+ para
}

