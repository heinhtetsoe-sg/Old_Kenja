function btn_submit(cmd) {
    //前年度コピー確認
    if (cmd == "copy") {
        if (confirm("{rval MSG101}") == false) {
            return false;
        }
    }
    //取消確認
    if (cmd == "reset") {
        if (confirm("{rval MSG106}") == false) {
            return false;
        }
    }

    if (check_checkBox() == false) {
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//順位出力、基準点のグレーアウト処理
function check_juni_gakushu(cmd) {
    if (document.forms[0].SEQ009.checked) {
        document.forms[0].SEQ0201.checked = true;
        document.forms[0].SEQ0211.checked = true;
        document.forms[0].SEQ0201.disabled = false;
        document.forms[0].SEQ0202.disabled = false;
        document.forms[0].SEQ0211.disabled = false;
        document.forms[0].SEQ0212.disabled = false;
    } else {
        document.forms[0].SEQ0201.checked = "";
        document.forms[0].SEQ0202.checked = "";
        document.forms[0].SEQ0211.checked = "";
        document.forms[0].SEQ0212.checked = "";
        document.forms[0].SEQ0201.checked = true;
        document.forms[0].SEQ0211.checked = true;
        document.forms[0].SEQ0201.disabled = true;
        document.forms[0].SEQ0202.disabled = true;
        document.forms[0].SEQ0211.disabled = true;
        document.forms[0].SEQ0212.disabled = true;
    }
    check_checkBox();
}

function check_juni_teiki() {
    if (document.forms[0].SEQ011.checked) {
        document.forms[0].SEQ0221.checked = true;
        document.forms[0].SEQ0231.checked = true;
        document.forms[0].SEQ0221.disabled = false;
        document.forms[0].SEQ0222.disabled = false;
        document.forms[0].SEQ0231.disabled = false;
        document.forms[0].SEQ0232.disabled = false;
    } else {
        document.forms[0].SEQ0221.checked = "";
        document.forms[0].SEQ0222.checked = "";
        document.forms[0].SEQ0231.checked = "";
        document.forms[0].SEQ0232.checked = "";
        document.forms[0].SEQ0221.checked = true;
        document.forms[0].SEQ0231.checked = true;
        document.forms[0].SEQ0221.disabled = true;
        document.forms[0].SEQ0222.disabled = true;
        document.forms[0].SEQ0231.disabled = true;
        document.forms[0].SEQ0232.disabled = true;
    }
}

function check_nyuryoku() {
    if (document.forms[0].SEQ0122.checked ||
        document.forms[0].SEQ0124.checked) {
        document.forms[0].SEQ013.disabled = true;
    } else {
        document.forms[0].SEQ013.disabled = false;
    }
}

function check_checkBox() {
    if (check_hissu() == false) {
        alert( "総点、個人平均、学級平均、順位のいずれかはチェックを入れてください");
        return false;
    }
    return true;
}

//総点・個人平均・学級平均・順位にチェックがついているか確認
function check_hissu() {
    var souten = document.forms[0].SEQ006.checked;
    var kozinheikin = document.forms[0].SEQ007.checked;
    var gakkyuheikin = document.forms[0].SEQ008.checked;
    var juni = document.forms[0].SEQ009.checked;

    if (souten       == false &&
        kozinheikin  == false &&
        gakkyuheikin == false &&
        juni         == false) {
        return false;
    }
    return true;
}
