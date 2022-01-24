function btn_submit(cmd) {
    if (cmd == 'reset'){
        result = confirm('{rval MSG106}');
        if (result == false)
            return false;
    }
    if (cmd == 'update'){
        var getgrade = document.forms[0].GETGRADE.value;
        
        //SCHREG_REGD_GDATデータチェック
        if (getgrade > 0 ) {
            result = confirm('{rval MSG102}');
            if (result == false) {
                return false;
            }
        } else {
            alert('{rval MSG305}' + '\n\n対象年度の学年名称が登録されていません。');
            return false;
        }
    }
    if (cmd == 'shokitiyear'){
        var getevent = document.forms[0].GETEVENT.value;
        var getgrade = document.forms[0].GETGRADE.value;
        var grade = document.forms[0].GRADE.value;
        
         //SCHREG_REGD_GDATデータチェック
        if (getgrade > 0) {
        
            //EVENT_MSTデータチェック(年度）
            if (getevent > 0 ) {
                result = confirm('{rval MSG104}');
                if (result == false)
                    return false;
            } else {
                result = confirm('{rval MSG102}');
                if (result == false)
                    return false;
            }
        } else {
            if (grade != '00') {
                alert('{rval MSG305}' + '\n\n対象年度の学年名称が登録されていません。');
                return false;
            
            } else {
                result = confirm('{rval MSG102}');
                if (result == false)
                    return false;
            }
        }
    }
    
    if (cmd == 'shokitimonth'){
        var geteventmonth = document.forms[0].GETEVENTMONTH.value;
        var getgrade = document.forms[0].GETGRADE.value;
        var grade = document.forms[0].GRADE.value;
        
         //SCHREG_REGD_GDATデータチェック
        if (getgrade > 0) {
        
            //EVENT_MSTデータチェック(指定月）
            if (geteventmonth > 0 ) {
                result = confirm('{rval MSG104}');
                if (result == false)
                    return false;
            } else {
                result = confirm('{rval MSG102}');
                if (result == false)
                    return false;
            }
        } else {
            if (grade != '00') {
                alert('{rval MSG305}' + '\n\n対象年度の学年名称が登録されていません。');
                return false;
            
            } else {
                result = confirm('{rval MSG102}');
                if (result == false)
                    return false;
            }
        }
    }

    if (cmd == 'monthmain'){
        var monthflg = document.forms[0].DATA_CHANGE_FLG.value;
        if (monthflg == "true") {
            result = confirm('{rval MSG108}');
            if (result == false)
                return false;
        }
    }

    //コピー
    if (cmd == 'copy_month' || cmd == 'copy_year') {
        var copy_grade  = document.forms[0].COPY_GRADE.value;
        var copy_major  = document.forms[0].COPY_MAJOR.value;
        var grade       = document.forms[0].GRADE.value;
        var school_kind = document.forms[0].SCHOOL_KIND.value;
        var major       = document.forms[0].COURSE_MAJOR.value;
        if (school_kind == 'P') {
            if (copy_grade == grade) {
                alert('コピー先が同じ学年です。');
                return false;
            }
        } else {
            if (copy_grade == grade && copy_major == major) {
                alert('コピー先が同じ学年・学科です。');
                return false;
            }
        }

        if (cmd == 'copy_year') {
            var eventcnt = document.forms[0].COPY_EVENT_CNT.value;
        } else {
            var eventcnt = document.forms[0].COPY_EVENT_CNT_MONTH.value;
        }
        if (eventcnt > 0) {
            result = confirm('{rval MSG104}');
            if (result == false)
                return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

// 権限
function closing_window()
{
    alert('{rval MSG300}');
    closeWin();
    return true;
}

//チェックボックス、テキストボックスのデータが変更されたときにセットされるフラグ
function dataFlgSet() {
    document.forms[0].DATA_CHANGE_FLG.value = "true";
}

//年度追加（コンボのみ追加）
function add(cmd) {
    if (cmd == 'year_add'){
        var temp1 = new Array();
        var tempa = new Array();
        var v = document.forms[0].YEAR.length;
        var w = document.forms[0].year_add.value

        if (w == "")
            return false;

        for (var i = 0; i < v; i++) {
            if (w == document.forms[0].YEAR.options[i].value) {
                alert("追加した年度は既に存在しています。");
                return false;
            }
        }
        document.forms[0].YEAR.options[v] = new Option();
        document.forms[0].YEAR.options[v].value = w;
        document.forms[0].YEAR.options[v].text = w;

        for (var i = 0; i < document.forms[0].YEAR.length; i++) {
            temp1[i] = document.forms[0].YEAR.options[i].value;
            tempa[i] = document.forms[0].YEAR.options[i].text;
        }
        //sort
        temp1 = temp1.sort();
        tempa = tempa.sort();
        temp1 = temp1.reverse();
        tempa = tempa.reverse();

        //generating new options
        ClearList(document.forms[0].YEAR,document.forms[0].YEAR);
        if (temp1.length>0) {
            for (var i = 0; i < temp1.length; i++) {
                document.forms[0].YEAR.options[i] = new Option();
                document.forms[0].YEAR.options[i].value = temp1[i];
                document.forms[0].YEAR.options[i].text =  tempa[i];
                if(w==temp1[i]) {
                    document.forms[0].YEAR.options[i].selected=true;
                }
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
