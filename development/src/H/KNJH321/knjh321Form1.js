function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//グラフへデータを渡す
function addDataToApplet() {
    document.chartApplet.setSelectedIndex(document.forms[0].cmbIndex.value);
    var allData = document.forms[0].adpara.value + ',';
    if (0 == document.forms[0].cmbIndex.value) {
        allData += document.forms[0].SUBTARGET1.value;
        allData += document.forms[0].SUBTARGET2.value;
        allData += document.forms[0].SUBTARGET3.value;
    } else {
        allData += document.forms[0].MOCKTARGET1.value;
        allData += document.forms[0].MOCKTARGET2.value;
        allData += document.forms[0].MOCKTARGET3.value;
    }
    allData = allData.substr(0, allData.length - 1);

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
