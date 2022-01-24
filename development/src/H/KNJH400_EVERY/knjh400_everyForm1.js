window.onload = function(){
}
function btn_submit(cmd) {
    if (cmd == "search") {
        if(document.forms[0].FROM_DATE.value != ""){
            var from = document.forms[0].FROM_DATE;
            var to   = document.forms[0].TO_DATE;
            var ctrldate = document.forms[0].CTRL_DATE;
            
            if(to.value != ""){
                if(from.value > to.value){
                    alert('不正な日付です。');
                    return false;
                }
            }else{
                if(from.value > ctrldate.value){
                    var date = ctrldate.value.replace('-', '年');
                        date = date.replace('-', '月');
                        date = date+"日";
                    alert(date+'以前の日付を選択してください。');
                    return false;
                }
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


//頻出タグ押したとき用
function clickTag(data)
{
    var tag = document.forms[0].TAGSEARCH.value;
    if(tag.substr(-1) == '　' || tag.substr(-1) == ' ' || tag == ''){
        document.forms[0].TAGSEARCH.value = document.forms[0].TAGSEARCH.value + data;
    }else{
        document.forms[0].TAGSEARCH.value = document.forms[0].TAGSEARCH.value + '　' + data;
    }
}
