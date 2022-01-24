function btn_submit(cmd) {
return false;//この区間は通らないと思う
    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].left_select.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function loadCheck(REQUESTROOT){

    var coursemajorcd = new Array();
    coursemajorcd = document.forms[0].CHECK_COURSEMAJORCD.value.split(',');


    var parent_coursecd         = coursemajorcd[0];
    var parent_majorcd          = coursemajorcd[1];
    var parent_coursecode       = document.forms[0].CHECK_COURSECODE.value;

    coursemajorcd = new Array();
    coursemajorcd = document.forms[0].COURSEMAJORCD.value.split(',');

    var this_coursecd         = coursemajorcd[0];
    var this_majorcd          = coursemajorcd[1];
    var this_coursecode       = document.forms[0].COURSECODE.value;

    var check_grade          = '0';
    var check_hr_class       = '0';
    var check_attendno       = '0';
    var check_annual         = '0';
    var check_coursecd       = parent_coursecd       != this_coursecd ? '1' : '0';
    var check_majorcd        = parent_majorcd        != this_majorcd ? '1' : '0';
    var check_coursecode     = parent_coursecode     != this_coursecode ? '1' : '0';
    var check_name           = '0';
    var check_name_show      = '0';
    var check_name_kana      = '0';
    var check_name_eng       = '0';
    var check_real_name      = '0';
    var check_real_name_kana = '0';

            load  = "loadwindow('"+ REQUESTROOT +"/A/KNJA110B/knja110bindex.php?cmd=subReplaceForm2";
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
            load += "',0,0,600,350)";

            eval(load);
}

function ShowConfirm() {
    if (!confirm('{rval MSG106}')){
        return false;
    }
}

function closing_window() {
        alert('{rval MSG300}');
        closeWin();
        return true;
}

function check_all(obj) {
    var ii = 0;
    re = new RegExp("RCHECK");
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (String(document.forms[0].elements[i].name).match(re)) {
            document.forms[0].elements[i].checked = obj.checked;
            ii++;
        }
    }
}

function doSubmit(cmd) {
    if (document.forms[0].RCHECK3.checked && document.forms[0].BIRTHDAY.value =="") {
        alert ('{rval MSG301}');
        return false;
    }
    if (document.forms[0].RCHECK4.checked && document.forms[0].ENT_DATE.value =="") {
        alert ('{rval MSG301}');
        return false;
    }
    if (document.forms[0].RCHECK10.checked && document.forms[0].GRD_DATE.value != '' &&
        (document.forms[0].CHK_GRD_SDATE.value > document.forms[0].GRD_DATE.value ||
         document.forms[0].CHK_GRD_EDATE.value < document.forms[0].GRD_DATE.value)
        ) {
        alert ('卒業日付範囲不正です。\n\n範囲：' + document.forms[0].CHK_GRD_SDATE.value + '～' + document.forms[0].CHK_GRD_EDATE.value);
        return false;
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].left_select.length==0 && document.forms[0].right_select.length==0) {
        alert('{rval MSG916}');
        return false;
    }
    for (var i = 0; i < document.forms[0].left_select.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    if (!confirm('一括更新では、履歴データの作成を行いません。')){
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}


function add() {
    var temp1 = new Array();
    var tempa = new Array();
    var v = document.forms[0].year.length;
    var w = document.forms[0].year_add.value;

    if (w == "")
        return false;

    for (var i = 0; i < v; i++) {
        if (w == document.forms[0].year.options[i].value) {
            alert("追加した年度は既に存在しています。");
            return false;
        }
    }
    document.forms[0].year.options[v] = new Option();
    document.forms[0].year.options[v].value = w;
    document.forms[0].year.options[v].text = w;

    for (var i = 0; i < document.forms[0].year.length; i++) {
        temp1[i] = document.forms[0].year.options[i].value;
        tempa[i] = document.forms[0].year.options[i].text;
    }
    //sort
    temp1 = temp1.sort();
    tempa = tempa.sort();
    temp1 = temp1.reverse();
    tempa = tempa.reverse();

    //generating new options
    ClearList(document.forms[0].year,document.forms[0].year);
    if (temp1.length>0) {
        for (var i = 0; i < temp1.length; i++)
        {
            document.forms[0].year.options[i] = new Option();
            document.forms[0].year.options[i].value = temp1[i];
            document.forms[0].year.options[i].text =  tempa[i];
            if(w==temp1[i]){
                document.forms[0].year.options[i].selected=true;
            }
        }
    }
    temp_clear();
}
