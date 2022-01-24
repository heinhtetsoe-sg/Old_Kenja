function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//グラフへデータを渡す
function addDataToApplet() {
    document.chartApplet.setSelectedIndex(document.forms[0].cmbIndex.value);
    var allData = document.forms[0].adpara.value;

    var subData = allData.split(',');
    for (var i = 0; i < subData.length; i++) {
        var data = subData[i].split('-');
        if ('' != data[2]) {
            document.chartApplet.addData(data[0], data[1], data[2]);
        } else {
            document.chartApplet.addData(data[0], data[1]);
        }
    }
}
