//jqury部
$(function(){
    // //ボタンとクラスを付与
    $("input[name='btn_end']").addClass("btn end");
    $("font").after("<input type='button' class='btn zaigaku' name='btn_zaigaku' value='在籍情報参照' onclick=wopen('../../O/KNJOSEARCH/index.php?PATH=/O/KNJO121/knjo121index.php&cmd=&target=KNJO121','search',0,0,700,500);>");

    //ボタン一括スタイル調整
    $(".btn").parent().css("text-align", "right").css("background-color", "#7379ae");

    //バリデーション用class付与
    $("td > input[name='SCHREGNO']").addClass("dup_schreg schreg");
    $("td > select[name='GRADE']").addClass("dup_grade grade");
    $("td > input[name='ATTENDNO']").addClass("dup_grade attend");
    $("td > select[name='KATEI']").addClass("katei");
    $("td > select[name='COURSE']").addClass("course");
    $("td > input[name='ANNUAL']").addClass("annual");
    $("td > input[name='ENTDATE']").addClass("entdate");
    $("td > select[name='RADIO']").addClass("radio");

    //error用class削除
    $(".dup_schreg").focusin(function() {
        $(".schreg_error").remove();
        $(".dup_schreg_error").remove();
    });
    $(".katei").focusin(function() {
        $(".katei_error").remove();
    });
    $(".course").focusin(function() {
        $(".course_error").remove();
    });
    $(".dup_grade").focusin(function() {
        $(".grade_error").remove();
        $(".dup_grade_error").remove();
    });
    $(".attend").focusin(function() {
        $(".attend_error").remove();
    });
    $(".annual").focusin(function() {
        $(".annual_error").remove();
    });
    $(".entdate").focusin(function() {
        $(".entdate_error").remove();
    });

   $("input[name='btn_read']").click(function(){

        var schregNo = $("input[name='SCHREGNO']").val();
        var grade    = $("select[name='GRADE']").val();
        var attendNo = $("input[name='ATTENDNO']").val();
        var katei    = $("select[name='KATEI']").val();
        var course   = $("select[name='COURSE']").val();
        var annual   = $("input[name='ANNUAL']").val();
        var entdate  = $("input[name='ENTDATE']").val();
        var radio    = $("input[name='RADIO']").val();

        var ctrlyear     = $("input[name='CTRL_YEAR']").val();
        var ctrlsemester = $("input[name='CTRL_SEMESTER']").val();
        
        var cnt;

        //各項目必須エラーメッセージ
        if(schregNo == '' || grade == '' || attendNo == '' || katei == '' || course == '' || annual == '' || entdate == '' || radio == ''){

            if(schregNo == ''){
                $(".schreg_error").remove();
                $(".schreg").after('<nobr class="schreg_error" style="color:red;">  ※入力してください。</nobr>');
            }
            if(katei == ''){
                $(".katei_error").remove();
                $(".katei").after('<nobr class="katei_error" style="color:red;">  ※選択してください。</nobr>');
            }
            if(course == ''){
                $(".course_error").remove();
                $(".course").after('<nobr class="course_error" style="color:red;">  ※選択してください。</nobr>');
            }
            if(grade == ''){
                $(".grade_error").remove();
                $(".grade").after('<nobr class="grade_error" style="color:red;">  ※選択してください。</nobr>');
            }
            if(attendNo == ''){
                $(".attend_error").remove();
                $(".attend").after('<nobr class="attend_error" style="color:red;">  ※入力してください。</nobr>');
            }
            if(annual == ''){
                $(".annual_error").remove();
                $(".annual").after('<nobr class="annual_error" style="color:red;">  ※入力してください。</nobr>');
            }
            if(entdate == ''){
                $(".entdate_error").remove();
                $(".entdate").after('<nobr class="entdate_error" style="color:red;">  ※入力してください。</nobr>');
            }
            return false;
        }

        $.ajax({
            type: "POST",
            url: "ajax.php",
            data: {
                "schregno": schregNo,
                "grade": grade,
                "attendno": attendNo,
                "radio" : radio,
                "year" : ctrlyear,
                "semester" : ctrlsemester
            },
            success: function(type){

                if(type == 2){
                    $(".dup_schreg_error").remove();
                    $(".dup_schreg").after('<nobr class="dup_schreg_error" style="color:red;">  ※学籍番号が重複しているため取り込めません。</nobr>');
                    return false;
                }
                if(type == 3){
                    $(".dup_grade_error").remove();
                    $(".dup_grade").after('<nobr class="dup_grade_error" style="color:red;">  ※年組番が重複しているため取り込めません。</nobr>');
                    return false;
                }
                if(type == 4){
                    if(!confirm('すでに取り込んだ指導要録データが存在しますが、取り込みなおしてよろしいですか?')){
                        return false;
                    }else{
                        document.forms[0].DELTYPE.value = "1";
                        btn_submit('delete');
                    }
                }else if(type == 5){
                    if(!confirm('すでに取り込んだ学齢簿情報が存在しますが、取り込みなおしてよろしいですか？')){
                        return false;
                    }else{
                        document.forms[0].DELTYPE.value = "2";
                        btn_submit('delete');
                    }
                }else if(type == 6){
                    if(!confirm('学齢簿の取込データから作成した児童生徒基礎情報を上書きますが、よろしいですか？')){
                        return false;
                    }else{
                        document.forms[0].DELTYPE.value = "3";
                        btn_submit('delete');
                    }
                }else if(type == 7){
                    if(!confirm('指導要録情報の取込データから作成した児童生徒基礎情報を上書きますが、よろしいですか？')){
                        return false;
                    }else{
                        document.forms[0].DELTYPE.value = "3";
                        btn_submit('delete');
                    }
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
//ラジオボタン押したらhiddenに値を入れる
function radio_change(radio)
{
    document.forms[0].RADIO.value = radio.value;
    btn_submit('main');
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
