//取込ボタン押し下げ時の処理
function btn_submit(seq) {
    var parentFrame = top.main_frame.right_frame.document;
    var remark = document.forms[0]['REMARK-' + seq].value;

    if (remark != "") {
        parentFrame.forms[0]['REMARK-1'].value = parentFrame.forms[0]['REMARK-1'].value + remark;
        parentFrame.forms[0]['REMARK-1'].style.backgroundColor = "#FFCCFF";
    }
    parent.closeit();
}