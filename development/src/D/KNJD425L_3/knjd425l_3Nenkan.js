//取込ボタン押し下げ時の処理
function btn_submit(subclasscd) {
    var parentFrame = top.main_frame.right_frame.document;
    var remark = document.forms[0]['REMARK_' + subclasscd].value;

    if (parentFrame.forms[0]['REMARK_' + subclasscd] && remark != "") {
        parentFrame.forms[0]['REMARK_' + subclasscd].value += remark;
    }
    parent.closeit();
}
