window.onload = function(){
    //グラフ作成
    var graph = document.forms[0].graph.value;
    for(i=0;i<graph;i++){
        document.getElementById("line"+i).style.display="none"
        createRadarChart("line", i);
    }

}
function btn_submit(cmd) {
    if(cmd == "csv"){
        var checkFlg = 0;
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var el = document.forms[0].elements[i];
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
