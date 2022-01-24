//取込ボタン押し下げ時の処理
function btn_submit(subclasscd) {
    var parentFrame = top.main_frame.right_frame.document;
    var remark = document.forms[0]['REMARK_' + subclasscd].value;

    if (remark != "") {
        parentFrame.forms[0]['REMARK_' + subclasscd].value = parentFrame.forms[0]['REMARK_' + subclasscd].value + remark;
        parentFrame.forms[0]['REMARK_' + subclasscd].style.backgroundColor = "#FFCCFF";
    }
    parent.closeit();
}