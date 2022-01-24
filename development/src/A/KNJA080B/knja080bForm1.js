function btn_submit(cmd) {   
    switch(cmd){
        case 'clear':
            if (!confirm('{rval MSG107}')) return true;
            break;
        case 'selectclass':
            with(document.forms[0]){
                NEW_CLASS_STU.options.length = 0;
                OLD_CLASS_STU.options.length = 0;
                document.getElementById("NEWNUM").innerHTML = NEW_CLASS_STU.options.length;
                document.getElementById("OLDNUM").innerHTML = OLD_CLASS_STU.options.length;
            }
            hiddenWin('knja080bindex.php?cmd=selectclass&OLDCLASS='+document.forms[0].OLDCLASS.value+'&NEWCLASS='+document.forms[0].NEWCLASS.value);
            return true;
        default:
            break;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit() {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].NEW_CLASS_STU.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].NEW_CLASS_STU.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;
}
function hiddenWin(url){
    //document.getElementById("dwindow").style.display="none"
    document.getElementById("cframe").src=url
}
//生徒移動
function moveStudent(side){
    var moveflg = true;
    var cnt = 0;
    var flg = "";
    new_div = document.forms[0].NEWCLASS.value.substring(0,1);
    old_div = document.forms[0].OLDCLASS.value.substring(0,1);
    if (new_div == "2" && old_div == "2"){ //TODO:仕様未確定のため両方ＨＲの時、留年チェックする
        new_grade = document.forms[0].NEWCLASS.value.substring(2,4);
        old_grade = document.forms[0].OLDCLASS.value.substring(2,4);
        if (side == "left" || side == "sel_add_all"){
            for (var i = 0; i < document.forms[0].OLD_CLASS_STU.options.length; i++){
                if (document.forms[0].OLD_CLASS_STU.options[i].selected && side == "left" || side == "sel_add_all"){
                    var val = document.forms[0].OLD_CLASS_STU.options[i].text;
                    if (val.match(/\[留\]/)){
                        cnt++;
                        if(cnt == 1 && new_grade == old_grade) {
                            flg = confirm('留年生が含まれています。よろしいですか？');
                        } else {
                            flg = alert('留年生の割振り先学年が違います。');
                        }
                        if (flg) {
                            moveflg = true;
                        } else {
                            moveflg = false;
                            break;
                        }
                    }
                }
            }
        }
    }
    if (moveflg){
        move(side,'NEW_CLASS_STU','OLD_CLASS_STU',1);
    }
    with(document.forms[0]){
        document.getElementById("NEWNUM").innerHTML = NEW_CLASS_STU.options.length;
        document.getElementById("OLDNUM").innerHTML = OLD_CLASS_STU.options.length;
    }
}
function closing_window(MSGCD)
{
    if (MSGCD == 'MSG300') {
        alert('{rval MSG300}');
    } else if (MSGCD == 'MSG300_A032') {
        //名称マスタ「A032」を使用する学校
        alert('{rval MSG300}'+'\n'+'プロパティ未設定のため処理できません。');
    }
    closeWin();
}
function start()
{
    hiddenWin('knja080bindex.php?cmd=selectclass&OLDCLASS='+document.forms[0].OLDCLASS.value+'&NEWCLASS='+document.forms[0].NEWCLASS.value);
    return true;
}