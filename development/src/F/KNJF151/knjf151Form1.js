function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//PDFファイルのダウンロード
function DownloadPdf(obj) {
    //ファイル名セット
    document.forms[0].PDF.value = obj.value;

    document.forms[0].cmd.value = 'download';
    document.forms[0].submit();
    return false;
}
