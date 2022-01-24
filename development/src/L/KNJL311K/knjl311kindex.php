<?php
require_once('knjl311kModel.inc');
require_once('knjl311kQuery.inc');

class knjl311kController extends Controller {
    var $ModelClassName = "knjl311kModel";
    var $ProgramID      = "KNJL311K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl311k":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl311kModel();       //コントロールマスタの呼び出し
                    $this->callView("knjl311kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl311kCtl = new knjl311kController;
var_dump($_REQUEST);
?>
