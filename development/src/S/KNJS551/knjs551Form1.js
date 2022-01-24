function btn_submit(cmd) {
    if (cmd == 'reset'){
        result = confirm('{rval MSG106}');
        if (result == false)
            return false;
    }
    if (cmd == 'update'){
        var getgrade = document.forms[0].GETGRADE.value;
        
        //SCHREG_REGD_GDATデータチェック
        if (getgrade == 0 ) {
            alert('{rval MSG305}' + '\n\n対象年度の学年名称が登録されていません。');
            return false;
        }
    }
    
    if (cmd == 'shokiti'){
        var getunitclass    = document.forms[0].UNITCLASSDATA.value;
        var getgrade        = document.forms[0].GETGRADE.value;
        
         //SCHREG_REGD_GDATデータチェック
        if (getgrade > 0) {
        
            //EVENT_MSTデータチェック(年度）
            if (getunitclass > 0 ) {
                result = confirm('{rval MSG104}');
                if (result == false)
                    return false;
            } else {
                result = confirm('{rval MSG102}');
                if (result == false)
                    return false;
            }
            
        } else {
            alert('{rval MSG305}' + '\n\n対象年度の学年名称が登録されていません。');
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

//学年表示用
function setSum(Scount, Gcount) {
    var subclassnum = parseInt(subclassnum);
    subclassnum = 0;
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var subclasstotal=document.getElementById("STANDARD_TOTAL_" + Scount);
        var obj_updElement = document.forms[0].elements[i];
        re = new RegExp("^STANDARD_TIME_" + Scount);
        
        if (obj_updElement.name.match(re)) {
            if (obj_updElement.value == "") {
                continue;
            }
            subclassnum += parseInt(obj_updElement.value);
        }
    }
    subclasstotal.innerHTML= subclassnum;
    setSum2(Scount, Gcount, subclassnum);
}

//教科表示用
function setSum2(Scount, Gcount, subclassnum) {
    var gradenum = parseInt(gradenum);
    gradenum = 0;
    
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var gradetotal=document.getElementById("GAKUEN_TOTAL_" + Gcount);
        var obj_updElement = document.forms[0].elements[i];
        re = new RegExp("^STANDARD_TIME_[0-9]+_" + Gcount);
        
        if (obj_updElement.name.match(re)) {
            if (obj_updElement.value == "") {
                continue;
            }
            gradenum += parseInt(obj_updElement.value);
        }
        
    }
    gradetotal.innerHTML= gradenum;
    setSum3(Scount, Gcount, subclassnum);
}

//合計表示用
function setSum3(Scount, Gcount, subclassnum) {
    var allnum = parseInt(allnum);
    allnum = 0;
    
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var alltotal=document.getElementById("STANDARD_TOTAL_ALL");
        var obj_updElement = document.forms[0].elements[i];
        
        re2 = new RegExp("^STANDARD_TIME_");
        
        if (obj_updElement.name.match(re2)) {
            if (obj_updElement.value == "") {
                continue;
            }
            allnum += parseInt(obj_updElement.value);
        }
    }
    alltotal.innerHTML= allnum;
}