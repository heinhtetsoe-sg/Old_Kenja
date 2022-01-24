function btn_submit(cmd) {
    if (cmd == 'copy' && !confirm('{rval MSG101}' + '\n※今年度にない、前年度データのみ処理します。')){
        return false;
    }
    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}'))
            return false;
        else
            cmd = "";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//更新
function doSubmit(cmd) {
    var attribute1 = document.forms[0].selectdata;
    attribute1.value = "";
    if (cmd == 'update'){
        if (document.forms[0].grade_input.length==0 && document.forms[0].grade_delete.length==0) {
            alert('{rval MSG916}');
            return false;
        }
        sep = "";
        for (var i = 0; i < document.forms[0].grade_input.length; i++)
        {
            attribute1.value = attribute1.value + sep + document.forms[0].grade_input.options[i].value;
            sep = ",";
        }
    }
    var attribute2Left = document.forms[0].selectdata2Left;
    attribute2Left.value = "";
    var attribute2Right = document.forms[0].selectdata2Right;
    attribute2Right.value = "";
    if (cmd == 'update2'){
        if (document.forms[0].attend_input.length==0 && document.forms[0].attend_delete.length==0) {
            alert('{rval MSG916}');
            return false;
        }
        sep = "";
        for (var i = 0; i < document.forms[0].attend_input.length; i++)
        {
            attribute2Left.value = attribute2Left.value + sep + document.forms[0].attend_input.options[i].value;
            sep = ",";
        }
        sep = "";
        for (var i = 0; i < document.forms[0].attend_delete.length; i++)
        {
            attribute2Right.value = attribute2Right.value + sep + document.forms[0].attend_delete.options[i].value;
            sep = ",";
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//更新（出欠）
function doSubmit2() {
    var attribute2 = document.forms[0].selectdata2;
    attribute2.value = "";
    if (document.forms[0].attend_more_input.length==0 && document.forms[0].attend_more_delete.length==0) {
        alert('{rval MSG916}');
        return false;
    }
    sep = "";
    for (var i = 0; i < document.forms[0].attend_more_input.length; i++) {
        attribute2.value = attribute2.value + sep + document.forms[0].attend_more_input.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = 'update3';
    document.forms[0].submit();
    return false;
}

//更新（実力テスト）
function doSubmit3() {
    var attribute3 = document.forms[0].selectdata3;
    attribute3.value = "";
    if (document.forms[0].proficiency_input.length==0 && document.forms[0].proficiency_delete.length==0) {
        alert('{rval MSG916}');
        return false;
    }
    sep = "";
    for (var i = 0; i < document.forms[0].proficiency_input.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].proficiency_input.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = 'update4';
    document.forms[0].submit();
    return false;
}

//更新（観点）
function doSubmitJview() {
    var attribute = document.forms[0].selectdataJview;
    attribute.value = "";
    if (document.forms[0].jview_input.length==0 && document.forms[0].jview_delete.length==0) {
        alert('{rval MSG916}');
        return false;
    }
    sep = "";
    for (var i = 0; i < document.forms[0].jview_input.length; i++) {
        attribute.value = attribute.value + sep + document.forms[0].jview_input.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = 'updateJview';
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function temp_clear()
{
    ClearList(document.forms[0].sectionyear,document.forms[0].sectionyear);
    ClearList(document.forms[0].sectionmaster,document.forms[0].sectionmaster);
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
