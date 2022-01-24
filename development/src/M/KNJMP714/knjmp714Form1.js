function btn_submit(cmd) {

    if (cmd == "update") {
        re = new RegExp("^UPDATE_CHK_" );
        var updDataUmu = false;
        for (var i=0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name.match(re) && document.forms[0].elements[i].checked) {
                updDataUmu = true;
                break;
            }
        }
        if (!updDataUmu) {
            alert('更新対象データが選択されていません。');
            return false;
        }
    }

    //サブミット中、更新ボタン使用不可
    document.forms[0].btn_update.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function popupGrp(schregNo, slipNo){
    loadwindow('knjmp714index.php?cmd=grpform&SEND_SCHREGNO=' + schregNo + '&SEND_SLIP_NO=' + slipNo, 0, 0, 700, 300);
}

function check_all(obj) {
    re = new RegExp("^UPDATE_CHK_" );
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(re) && !document.forms[0].elements[i].disabled) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

function changeJugyouRyou(obj, schregNo) {

    var groupCd = document.forms[0]["GROPCD_" + schregNo].value;
    var syokeihi = 0;
    if (groupCd != "") {
        syokeihi = document.forms[0]["GROUP_" + groupCd].value;
    }
    var credits = document.forms[0]["COLLECT_CNT_" + schregNo].value;
    var jugyouryou = document.forms[0]["JUGYOU_" + schregNo].value;
    if ((credits == "" || jugyouryou == "") && groupCd == "") {
        document.getElementById('JUGYOURYOU_DISP_' + schregNo).innerHTML = "";
        document.getElementById('SYOKEIHI_DISP_' + schregNo).innerHTML = "";
        document.getElementById('TMONEY_DISP_' + schregNo).innerHTML = "";
        return false;
    }
    var setMoney = jugyouryou == "" ? 0 : document.forms[0]["JUGYOU_" + jugyouryou].value;
    credits = credits == "" ? 0 : credits;
    syokeihi = syokeihi == "" ? 0 : syokeihi;
    var jugyouMoney = parseInt(setMoney) * parseInt(credits);
    document.getElementById('JUGYOURYOU_DISP_' + schregNo).innerHTML = number_format(jugyouMoney);
    var syokeihiMoney = parseInt(syokeihi);
    document.getElementById('SYOKEIHI_DISP_' + schregNo).innerHTML = number_format(syokeihiMoney);
    var totalMoney = parseInt(syokeihi) + parseInt(setMoney) * parseInt(credits);
    document.getElementById('TMONEY_DISP_' + schregNo).innerHTML = number_format(totalMoney);

    changeTotalMoney(schregNo);
    return false;
}

//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
//スクロール
function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}
