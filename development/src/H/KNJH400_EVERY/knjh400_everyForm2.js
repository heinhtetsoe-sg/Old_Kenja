window.onload = function(){
    if(document.forms[0].cmd.value == 'add_comp' || document.forms[0].cmd.value == 'up_comp'){
        parent.frames['left_frame'].document.forms[0].cmd.value = 'search';
        parent.frames['left_frame'].document.forms[0].submit();
        //window.open('knjh400_everyindex.php?cmd=search','left_frame');
        //window.open('knjh400_everyindex.php?cmd=choice','right_frame');
    }else if(document.forms[0].cmd.value == 'del_comp'){
        parent.frames['left_frame'].document.forms[0].cmd.value = 'search';
        parent.frames['left_frame'].document.forms[0].submit();
        //window.open('knjh400_everyindex.php?cmd=search','left_frame');
        //window.open('knjh400_everyindex.php?cmd=new','right_frame');
    }
    
}

function btn_submit(cmd) {
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }
    
    if(cmd == 'add' || cmd == 'update'){
        if(document.forms[0].DATE.value == ""){
            alert('日付を選択してください。');
            document.forms[0].DATE.focus();
            return false;
        }
        if(document.forms[0].H_TIME.value == ""){
            alert('時間を指定してください。');
            document.forms[0].H_TIME.focus();
            return false;
        }
        if(document.forms[0].H_TIME.value > 23){
            alert('時間の指定範囲外です。');
            document.forms[0].H_TIME.focus();
            return false;
        }
        if(document.forms[0].M_TIME.value > 59){
            alert('時間の指定範囲外です。');
            document.forms[0].M_TIME.focus();
            return false;
        }
        if(document.forms[0].TITLE.value == ""){
            alert('件名と内容は入力必須項目です。');
            document.forms[0].TITLE.focus();
            return false;
        }
        if(document.forms[0].TEXT.value == ""){
            alert('件名と内容は入力必須項目です。');
            document.forms[0].TEXT.focus();
            return false;
        }
    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//頻出タグ押したとき用
function clickTag(data)
{
    var tag = document.forms[0].TAG.value;
    if(tag.substr(-1) == '　' || tag.substr(-1) == ' ' || tag == ''){
        document.forms[0].TAG.value = document.forms[0].TAG.value + data;
    }else{
        document.forms[0].TAG.value = document.forms[0].TAG.value + '　' + data;
    }
}
