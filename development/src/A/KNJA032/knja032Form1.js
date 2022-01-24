function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function check_all(){
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]") {
            document.forms[0].elements[i].checked = document.forms[0]['CHECKALL'].checked;
            if (document.forms[0].elements[i].checked == true){
                chgColor(document.forms[0].elements[i].value, "#ccffcc");
            }else{
                chgColor(document.forms[0].elements[i].value, "#ffffff");
            }
        }
    }
}
function chgTrans(obj){
    if (parseInt(obj.value) == 9){
        document.forms[0].btn_judge.disabled = true;
    }else{
        document.forms[0].btn_judge.disabled = false;
    }
}
function chgColor(schregno, rgb){
    document.getElementById(schregno).style.background = rgb;
}
function btn_read(){
    
    if (document.forms[0].GRADE_FEARVAL.value == ''){
        alert('学年末保留値が設定されていません。');
    }else if (document.forms[0].GRAD_CREDITS.value == ''){
        alert('卒業単位数が設定されていません。');
    }
    var g_c = document.forms[0].GET_CREDITS.value.split(",");
    var r_c = document.forms[0].REM_CREDITS.value.split(",");
    var grade = document.forms[0].gc_select.value.split(",");

    var schregno, html;
    for (var i = 0; i < document.forms[0]["CHECKED\[\]"].length; i++){
        schregno = document.forms[0]["CHECKED\[\]"][i].value;
        chgColor(schregno, "#ffffff");
        if (document.forms[0].SCHOOLDIV.value == 0){    //学年制
            switch(parseInt(document.forms[0].TRANS.value)){
                case 1:     //進級
//                    html = "進級";
                case 2:     //卒業
                    if (parseInt(document.forms[0].TRANS.value) == 2){
//                        html = "卒業";
                    }
                    if (parseInt(r_c[i]) < parseInt(document.forms[0].GRADE_FEARVAL.value)){
//                        outputLAYER("TARGET_"+schregno, html);

                        document.forms[0]["CHECKED\[\]"][i].checked = true;
                    }else{
//                        outputLAYER("TARGET_"+schregno, "");
                        document.forms[0]["CHECKED\[\]"][i].checked = false;
                    }
                    break;
                case 3:     //留年
                    if (parseInt(r_c[i]) >= parseInt(document.forms[0].GRADE_FEARVAL.value)){
//                        outputLAYER("TARGET_"+schregno, "留年");
                        chgColor(schregno, "#ccffcc");
                        document.forms[0]["CHECKED\[\]"][i].checked = true;
                    }else{
//                        outputLAYER("TARGET_"+schregno, "");
                        document.forms[0]["CHECKED\[\]"][i].checked = false;
                    }
                    break;
                case 9:     //取消
            }
        }else{  //単位制
            if (parseInt(document.forms[0].TRANS.value) == 2 && parseInt(g_c[i]) >= parseInt(document.forms[0].GRAD_CREDITS.value)){
                document.forms[0]["CHECKED\[\]"][i].checked = true;
            }else{
                document.forms[0]["CHECKED\[\]"][i].checked = false;
            }
        }
        if (document.forms[0]["CHECKED\[\]"][i].checked == true)
        chgColor(schregno, "#ccffcc");
    }
}
function chkClick(obj){
    if (obj.checked == true){
        chgColor(obj.value, "#ccffcc");
    }else{
        chgColor(obj.value, "#ffffff");
    }
}
function closing_window(MSGCD, msg)
{
    if (MSGCD == 'MSG311'){
        alert('{rval MSG311}');
    }else if (MSGCD == 'MSG305'){
        alert('{rval MSG305}'+'\n'+msg);
    }
    closeWin();
    return true;
}

function showConfirm()
{
}
