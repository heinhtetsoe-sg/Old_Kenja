window.onload = function(){
    //グラフ作成
    document.getElementById("bar0").style.display="none"
    createRadarChart("stacked", 0);

}
function btn_submit(cmd) {
    if(cmd == "csv"){
        var checkFlg = 0;
        for (var i = 0; i < document.edit.elements.length; i++) {
            var el = document.edit.elements[i];
            if(el.name == "CHECK[]"){
                if(el.checked == true){
                    var checkFlg = 1;
                    break;
                }
            }
        }
        if(checkFlg == 0){
            alert('教科を選択してください。');
            return false;
        }
    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_ctrls(e) {
    document.forms[0].btn_add.disabled    = false;
    document.forms[0].btn_update.disabled = false;
    document.forms[0].btn_reset.disabled  = false;
}

function Page_jumper(jump,no)
{
    var cd;
    cd = '?NO=';

        parent.location.replace(jump + cd + no);

}

function Btn_reset(cmd) {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    }
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
