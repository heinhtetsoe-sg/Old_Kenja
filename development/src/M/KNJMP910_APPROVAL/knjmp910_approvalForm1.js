//サブミット
function btn_submit(cmd)
{
    var approvalFlg = document.forms[0].OUTGO_APPROVAL.checked;
    var cancelFlg = document.forms[0].OUTGO_CANCEL.checked;
    var approvalvalue = document.forms[0].OUTGO_APPROVAL_VALUE.value;
    var cancelvalue = document.forms[0].OUTGO_CANCEL_VALUE.value;
    var HenkinApproval = document.forms[0].GET_HENKIN_APPROVAL.value;
    var outgocheck1Flg   = document.forms[0].OUTGO_CHECK1.checked;
    var outgocheck1date  = document.forms[0].OUTGO_CHECK1_DATE.value;
    var outgocheck1staff = document.forms[0].OUTGO_CHECK1_STAFF.value;
    
    var outgodate = document.forms[0].OUTGO_DATE.value;

    if (cmd == "update") {
        if (outgocheck1Flg == "1") {
            if (outgocheck1date == "") {
                alert('{rval MSG203}' + '\n領収日を入力して下さい。');
                return false;
            }
            if (outgocheck1staff == "") {
                alert('{rval MSG203}' + '\n受取人氏名を選択して下さい。');
                return false;
            }
        }
        if (approvalvalue == "" && cancelFlg == "1") {
            alert('未決裁のキャンセルはできません。');
            return false;
        }
        if (approvalvalue == "1" && cancelvalue == "1" && cancelFlg == "") {
            alert('キャンセル後の取り消しはできません。');
            return false;
        } 
        if (approvalvalue == "" && cancelvalue == "" && approvalFlg == "1" && cancelFlg == "1") {
            alert('決裁済みとキャンセル両方にチェックが入っています。');
            return false;
        } 
        if (approvalvalue == "1" && cancelFlg == "" && approvalFlg == "") {
            alert('決裁の取り消しはキャンセルにチェックを入れてください。');
            return false;
        }
        if (approvalvalue == "1" && cancelvalue == "" && cancelFlg == "1") {
            //返金実行チェック
            if(HenkinApproval != "") {
                alert('{rval MSG203}' + '\n返金処理済みのため、キャンセルすることができません。');
                return false;
            }
            if (!confirm('{rval MSG102}' + '\nキャンセル実行後の取り消しはできません。')) {
                return false;
            }
        }
        if (approvalvalue == "" && cancelvalue == "" && approvalFlg == "1") {
            if (outgodate == "") {
                alert('{rval MSG203}' + '\n支出日を入力して下さい。');
                return false;
            }
            if (!confirm('{rval MSG102}' + '\n決裁実行後の支出金額の変更はできません。')) {
                return false;
            }
        }
        //決裁後の更新
        if (approvalvalue == "1" && cancelvalue == "" && approvalFlg == "1") {
            if (outgodate == "") {
                alert('{rval MSG203}' + '\n支出日を入力して下さい。');
                return false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}