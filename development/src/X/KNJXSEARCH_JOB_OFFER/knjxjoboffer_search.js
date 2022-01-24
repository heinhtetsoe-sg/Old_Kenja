
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function apply_name() {
    top.document.forms[0]['FS_CD'].value = '000001';
    top.closeit();
}
function getFrame() {
    return document.forms[0].frame.value;
}
function apply_joboffer(obj) {
    if (obj.selectedIndex >= 0) {
        var val = obj.options[obj.selectedIndex].text;
        var arr = val.split(" | ");

        var senkou_no = parent.document.forms[0].SENKOU_NO;
        var stat_cd = parent.document.forms[0].STAT_CD;
        var stat_cd_disp = parent.document.getElementById('STAT_CD');
        var stat_name = parent.document.getElementById('STAT_NAME');
        var zipcd = parent.document.getElementById('ZIPCD');
        var addr1 = parent.document.getElementById('ADDR1');
        var addr2 = parent.document.getElementById('ADDR2');
        var telno = parent.document.getElementById('TELNO');
        var industry_mname = parent.document.getElementById('INDUSTRY_MNAME');

        //求人CD
        if (senkou_no) {
            senkou_no.value = arr[0];
        }
        //会社コードセット
        if (stat_cd) {
            stat_cd.value = arr[7];
        }
        //会社コード表示
        if (stat_cd_disp) {
            stat_cd_disp.innerHTML = arr[7];
        }
        //会社名
        if (stat_name) {
            stat_name.innerHTML = arr[8];
        }
        //郵便番号
        if (zipcd) {
            zipcd.innerHTML = arr[9];
        }
        //住所1
        if (addr1) {
            addr1.innerHTML = arr[10];
        }
        //住所2
        if (addr2) {
            addr2.innerHTML = arr[11];
        }
        //電話番号
        if (telno) {
            telno.innerHTML = arr[12];
        }
        //産業種別名
        if (industry_mname) {
            industry_mname.innerHTML = arr[13];
        }
        parent.closeit();
    } else {
        alert("データが選択されていません");
    }

}
