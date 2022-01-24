//サブミット
function btn_submit(cmd)
{
    var approvalFlg = document.forms[0].INCOME_APPROVAL.checked;
    var cancelFlg = document.forms[0].INCOME_CANCEL.checked;
    var approvalvalue = document.forms[0].INCOME_APPROVAL_VALUE.value;
    var cancelvalue = document.forms[0].INCOME_CANCEL_VALUE.value;
    var incomedate = document.forms[0].INCOME_DATE.value;
    //金額
    var requestGk = document.forms[0].REQUEST_GK.value;
    var outgoSumGk = document.forms[0].OUTGO_SUM_GK.value;
    requestGk = (isNaN(requestGk) || !requestGk) ? 0 : parseInt(requestGk, 10);
    outgoSumGk = (isNaN(outgoSumGk) || !outgoSumGk) ? 0 : parseInt(outgoSumGk, 10);

    if (cmd == "update") {
        if (approvalvalue == "" && cancelFlg == "1") {
            alert('未決裁のキャンセルはできません。');
            return false;
        }
        if (approvalvalue == "" && cancelvalue == "" && approvalFlg == "1" && cancelFlg == "1") {
            alert('決裁済みとキャンセル両方にチェックが入っています。');
            return false;
        } 
        if (approvalvalue == "1" && cancelvalue == "" && cancelFlg == "1") {
            //金額チェック
            if(outgoSumGk - requestGk < 0) {
                alert('{rval MSG203}' + '\nキャンセル対象の収入金額が支出額の合計を上回っているため、キャンセルすることができません。');
                return false;
            }
        }
        if (approvalvalue == "" && cancelvalue == "" && approvalFlg == "1") {
            if (incomedate == "") {
                alert('{rval MSG203}' + '\n収入日を入力して下さい。');
                return false;
            }
            if (!confirm('{rval MSG102}' + '\n決裁実行後の収入金額の変更はできません。')) {
                return false;
            }
        }
        //決裁後の更新
        if (approvalvalue == "1" && cancelvalue == "" && approvalFlg == "1") {
            if (incomedate == "") {
                alert('{rval MSG203}' + '\n収入日を入力して下さい。');
                return false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}