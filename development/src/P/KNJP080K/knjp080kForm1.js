function btn_submit(cmd) {
    if (cmd == "exec" && document.forms[0].selectdata.value == "") {
        alert("データを選択してください");
        return;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
}
var opt = new Array();
function init(){
    for (var i = 0; i < document.forms[0].class_all.length; i++) {
        var v = document.forms[0].class_all.options[i].value;
        var t = document.forms[0].class_all.options[i].text;
        opt[v] = t;
    }
    chgGrade();
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
window.onload = init