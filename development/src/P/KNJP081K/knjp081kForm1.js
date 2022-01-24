function btn_submit(cmd) {

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].class_sel.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].class_sel.options[i].value;
        sep = ",";
    }

    attribute4 = document.forms[0].selectdataSch;
    attribute4.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].schreg_sel.length; i++) {
        attribute4.value = attribute4.value + sep + document.forms[0].schreg_sel.options[i].value;
        sep = ",";
    }

    if (cmd == "exec" && document.forms[0].selectdata.value == "") {
        alert("データを選択してください");
        return;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
}
var opt = new Array();
var optSch = new Array();
function init(){
    for (var i = 0; i < document.forms[0].class_all.length; i++) {
        var v = document.forms[0].class_all.options[i].value;
        var t = document.forms[0].class_all.options[i].text;
        opt[v] = t;
    }
    chgGrade();

    for (var i = 0; i < document.forms[0].schreg_all.length; i++) {
        var v = document.forms[0].schreg_all.options[i].value;
        var t = document.forms[0].schreg_all.options[i].text;
        optSch[v] = t;
    }
    chgSchreg();
}
function chgGrade(){
    var opt_left = new Array();
    for (var i = 0; i < document.forms[0].class_sel.length; i++) {
        var v = document.forms[0].class_sel.options[i].value;
        opt_left[v] = true;
    }
    document.forms[0].class_all.length = 0;
    var i = 0;
    for (var v in opt) {
        var a = v.split("-");
        if (document.forms[0].GRADE.value == a[0] && typeof opt_left[v] == "undefined") {
            document.forms[0].class_all.options[i] = new Option(opt[v],v);
            i++;
        }
    }
}
function chgSchreg(){
    var opt_left = new Array();
    for (var i = 0; i < document.forms[0].schreg_sel.length; i++) {
        var v = document.forms[0].schreg_sel.options[i].value;
        opt_left[v] = true;
    }
    document.forms[0].schreg_all.length = 0;
    var i = 0;
    for (var v in optSch) {
        var a = v.split("-");
        if (document.forms[0].GRADE_HR.value == a[0] + '-' + a[1] && typeof opt_left[v] == "undefined") {
            document.forms[0].schreg_all.options[i] = new Option(optSch[v], v);
            i++;
        }
    }
}
window.onload = init