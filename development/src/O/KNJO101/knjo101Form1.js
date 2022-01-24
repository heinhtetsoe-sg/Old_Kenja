//jqury部
$(function(){
   $("input[name='btn_read']").click(function(){

        var schregNo = $("input[name='SCHREGNO']").val();
        var grade    = $("select[name='GRADE']").val();
        var attendNo = $("input[name='ATTENDNO']").val();
        var katei    = $("select[name='KATEI']").val();
        var course   = $("select[name='COURSE']").val();
        var annual   = $("input[name='ANNUAL']").val();
        var entdate  = $("input[name='ENTDATE']").val();

        var ctrlyear     = $("input[name='CTRL_YEAR']").val();
        var ctrlsemester = $("input[name='CTRL_SEMESTER']").val();
        
        var cnt;

        if(schregNo == '' || grade == '' || attendNo == '' || katei == '' || course == '' || annual == '' || entdate == ''){
            alert('すべての項目を選択・入力してください。');
            return false;
        }
        
        $.ajax({
            type: "POST",
            url: "ajax.php",
            data: {
                "schregno": schregNo,
                "grade": grade,
                "attendno": attendNo,
                "year" : ctrlyear,
                "semester" : ctrlsemester
            },
            success: function(type){
            
                if(type == 1){
                    alert('指定した年組番が重複しているため、取り込みなおせません。');
                    return false;
                }else if(type == 2){
                    if(!confirm('すでに取込データが存在しますが、上書きしてよろしいですか？')){
                        return false;
                    }else{
                        btn_submit('delete');
                    }
                }else if(type == 3){
                    alert('すでに使用されている学籍番号のため、取り込めません。');
                    return false;
                }else if(type == 4){
                    alert('年組番が重複しているため、取り込めません。');
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
