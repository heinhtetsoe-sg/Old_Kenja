//jqury部
$(function(){
   $("input[name='btn_read']").click(function(){

        var schregNo = $("input[name='SCHREGNO']").val();

        var ctrlyear     = $("input[name='CTRL_YEAR']").val();
        var ctrlsemester = $("input[name='CTRL_SEMESTER']").val();
        
        var cnt;

        if(schregNo == ''){
            alert('学籍番号を入力してください。');
            return false;
        }
        
        $.ajax({
            type: "POST",
            url: "ajax.php",
            data: {
                "schregno": schregNo,
                "year" : ctrlyear,
                "semester" : ctrlsemester
            },
            success: function(type){
            
                if(type == 2){
                    if(!confirm('すでに取込データが存在しますが、上書きしてよろしいですか？')){
                        return false;
                    }else{
                        btn_submit('delete');
                    }
                }else if(type == 3){
                    if(!confirm('指導要録情報の取込データが存在しませんが、\n健康診断票情報にはすでに取込データが存在します。\n\n上書きしてよろしいですか？')){
                        return false;
                    }else{
                        btn_submit('delete');
                    }
                }else if(type == 4){
                    if(!confirm('指導要録情報の取込データが存在しませんが、取り込んでよろしいですか？')){
                        return false;
                    }else{
                        btn_submit('read');
                    }
                }else if(type == 5){
                    alert('生徒の基本情報を作成してから取り込んでください。');
                    return false;
               }else{
                    btn_submit('read');
                }
            }
        });
   });

});
function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";
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

//セキュリティーチェック
function OnSecurityError()
{
    alert('{rval MSG300}' + '\n高セキュリティー設定がされています。');
    closeWin();
}
//印刷
function newwin(SERVLET_URL, schoolCd, fileDiv) {
    document.forms[0].encoding = "application/x-www-form-urlencoded";
//    if (document.forms[0].OUTPUT[3].checked != true) {
//        btn_submit('exec');
//    } else {
        //テンプレート格納場所
        urlVal = document.URL;
        urlVal = urlVal.replace("http://", "");
        var resArray = urlVal.split("/");
        var fieldArray = fileDiv.split(":");
        urlVal = "/usr/local/" + resArray[1] + "/src/etc_system/XLS_TEMP_" + schoolCd + "/CSV_Template" + fieldArray[0] + "." + fieldArray[1];
        document.forms[0].TEMPLATE_PATH.value = urlVal;

        action = document.forms[0].action;
        target = document.forms[0].target;

    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
        document.forms[0].action = SERVLET_URL +"/KNJA";
        document.forms[0].target = "_blank";
        document.forms[0].submit();

        document.forms[0].action = action;
        document.forms[0].target = target;
//    }
}
