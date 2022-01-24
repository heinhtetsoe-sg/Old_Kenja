function btn_submit(cmd) {
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }
    if (cmd == 'subEnd') {
        return false;
    }
    if (cmd == 'subUpdate') {
        return false;
    }
    if (document.forms[0].GRD_DATE.value != '' &&
        (document.forms[0].CHK_GRD_SDATE.value > document.forms[0].GRD_DATE.value ||
         document.forms[0].CHK_GRD_EDATE.value < document.forms[0].GRD_DATE.value)
        ) {
        alert ('卒業日付範囲不正です。\n\n範囲：' + document.forms[0].CHK_GRD_SDATE.value + '～' + document.forms[0].CHK_GRD_EDATE.value);
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    return false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}

function loadCheck(REQUESTROOT){

    var grade_class = new Array();
    grade_class = document.forms[0].CHECK_GRADE_CLASS.value.split(',');

    var coursemajorcd = new Array();
    coursemajorcd = document.forms[0].CHECK_COURSEMAJORCD.value.split(',');


    var parent_grade            = grade_class[0];
    var parent_hr_class         = grade_class[1];
    var parent_coursecd         = coursemajorcd[0];
    var parent_majorcd          = coursemajorcd[1];
    var parent_attendno         = document.forms[0].CHECK_ATTENDNO.value;
    var parent_annual           = document.forms[0].CHECK_ANNUAL.value;
    var parent_coursecode       = document.forms[0].CHECK_COURSECODE.value;
    var parent_name             = document.forms[0].CHECK_NAME.value;
    var parent_name_show        = document.forms[0].CHECK_NAME_SHOW.value;
    var parent_name_kana        = document.forms[0].CHECK_NAME_KANA.value;
    var parent_name_eng         = document.forms[0].CHECK_NAME_ENG.value;
    var parent_real_name        = document.forms[0].CHECK_REAL_NAME.value;
    var parent_real_name_kana   = document.forms[0].CHECK_REAL_NAME_KANA.value;
    var parent_handicap         = document.forms[0].CHECK_HANDICAP.value;


    grade_class = new Array();
    grade_class = document.forms[0].GRADE_CLASS.value.split(',');

    coursemajorcd = new Array();
    coursemajorcd = document.forms[0].COURSEMAJORCD.value.split(',');

    var this_grade            = grade_class[0];
    var this_hr_class         = grade_class[1];
    var this_coursecd         = coursemajorcd[0];
    var this_majorcd          = coursemajorcd[1];
    var this_attendno         = document.forms[0].ATTENDNO.value;
    var this_annual           = document.forms[0].ANNUAL.value;
    var this_coursecode       = document.forms[0].COURSECODE.value;
    var this_name             = document.forms[0].NAME.value;
    var this_name_show        = document.forms[0].NAME_SHOW.value;
    var this_name_kana        = document.forms[0].NAME_KANA.value;
    var this_name_eng         = document.forms[0].NAME_ENG.value;
    var this_real_name        = document.forms[0].REAL_NAME.value;
    var this_real_name_kana   = document.forms[0].REAL_NAME_KANA.value;
    var this_handicap         = document.forms[0].HANDICAP.value;

    var check_grade          = parent_grade          != this_grade ? '1' : '0';
    var check_hr_class       = parent_hr_class       != this_hr_class ? '1' : '0';
    var check_attendno       = parent_attendno       != this_attendno ? '1' : '0';
    var check_annual         = parent_annual         != this_annual ? '1' : '0';
    var check_coursecd       = parent_coursecd       != this_coursecd ? '1' : '0';
    var check_majorcd        = parent_majorcd        != this_majorcd ? '1' : '0';
    var check_coursecode     = parent_coursecode     != this_coursecode ? '1' : '0';
    var check_name           = parent_name           != this_name ? '1' : '0';
    var check_name_show      = parent_name_show      != this_name_show ? '1' : '0';
    var check_name_kana      = parent_name_kana      != this_name_kana ? '1' : '0';
    var check_name_eng       = parent_name_eng       != this_name_eng ? '1' : '0';
    var check_real_name      = parent_real_name      != this_real_name ? '1' : '0';
    var check_real_name_kana = parent_real_name_kana != this_real_name_kana ? '1' : '0';
    var check_handicap       = parent_handicap       != this_handicap ? '1' : '0';

    var check_nationality2               = document.forms[0].NATIONALITY2_FLG.value == '1' ? '1' : '0';
    var check_nationality_name           = document.forms[0].NATIONALITY_NAME_FLG.value == '1' ? '1' : '0';
    var check_nationality_name_kana      = document.forms[0].NATIONALITY_NAME_KANA_FLG.value == '1' ? '1' : '0';
    var check_nationality_name_eng       = document.forms[0].NATIONALITY_NAME_ENG_FLG.value == '1' ? '1' : '0';
    var check_nationality_real_name      = document.forms[0].NATIONALITY_REAL_NAME_FLG.value == '1' ? '1' : '0';
    var check_nationality_real_name_kana = document.forms[0].NATIONALITY_REAL_NAME_KANA_FLG.value == '1' ? '1' : '0';

            load  = "loadwindow('"+ REQUESTROOT +"/A/KNJL510A/knjl510aindex.php?cmd=subForm2";
            load += "&SCHREGNO_FLG="+   document.forms[0].SCHREGNO.value;
            load += "&GRADE_FLG="+      check_grade       ;
            load += "&HR_CLASS_FLG="+   check_hr_class    ;
            load += "&ATTENDNO_FLG="+   check_attendno    ;
            load += "&ANNUAL_FLG="+     check_annual      ;
            load += "&COURSECD_FLG="+   check_coursecd    ;
            load += "&MAJORCD_FLG="+    check_majorcd     ;
            load += "&COURSECODE_FLG="+ check_coursecode  ;
            load += "&NAME_FLG="+       check_name        ;
            load += "&NAME_SHOW_FLG="+  check_name_show   ;
            load += "&NAME_KANA_FLG="+  check_name_kana   ;
            load += "&NAME_ENG_FLG="+   check_name_eng    ;
            load += "&REAL_NAME_FLG="+  check_real_name   ;
            load += "&REAL_NAME_KANA_FLG="+  check_real_name_kana   ;
            load += "&HANDICAP_FLG="+  check_handicap   ;
            load += "&NATIONALITY2_FLG="+       check_nationality2        ;
            load += "&NATIONALITY_NAME_FLG="+  check_nationality_name   ;
            load += "&NATIONALITY_NAME_KANA_FLG="+  check_nationality_name_kana   ;
            load += "&NATIONALITY_NAME_ENG_FLG="+   check_nationality_name_eng    ;
            load += "&NATIONALITY_REAL_NAME_FLG="+  check_nationality_real_name   ;
            load += "&NATIONALITY_REAL_NAME_KANA_FLG="+  check_nationality_real_name_kana   ;

            load += "',0,0,600,350)";

eval(load);
}

function Page_jumper(link) {
    if (document.forms[0].UPDATED1.value == "") {
        alert('リストから生徒を選択してください。');
        return;
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}

function Name_Clip(name_text){
    var str = name_text.value;
    var Cliping_str;

    Cliping_str = str.slice(0,10);

    if(document.forms[0].NAME_SHOW.value == '') document.forms[0].NAME_SHOW.value = Cliping_str;

    return true;
}

function closing_window(flg) {
    if (flg == 1) {
        alert('{rval MSG300}');
    }
    if (flg == 2) {
        alert('{rval MSG305}' + '\n(HRクラス作成、課程マスタ、学科マスタ、出身学校マスタ、名称マスタのいずれか)');
    }
    closeWin();
    return true;
}

function setWareki(obj, ymd) {
    var d = ymd;
    var tmp = d.split('/');
    var ret = Calc_Wareki(tmp[0],tmp[1],tmp[2]);
}

//学籍番号チェック
function checkSchregno(obj) {

    //英数字チェック
    obj.value = toAlphaNumber(obj.value);

    if (document.forms[0].useAutoNumbering.value == "1") {
        if (obj.value) {
            document.forms[0].btn_numbering.disabled = true;
        } else {
            document.forms[0].btn_numbering.disabled = false;
        }
    }

    return true;
}

//自動付番
function auto_numbering(obj, maxschno) {
    if (document.forms[0].SCHREGNO.value == "") {
        document.forms[0].SCHREGNO.value = maxschno;
        obj.disabled = true;
    }

    return true;
}
