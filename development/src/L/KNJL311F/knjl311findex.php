<?php

require_once('for_php7.php');

require_once('knjl311fModel.inc');
require_once('knjl311fQuery.inc');

class knjl311fController extends Controller {
    var $ModelClassName = "knjl311fModel";
    var $ProgramID      = "KNJL311F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl311f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl311fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl311fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl311fCtl = new knjl311fController;
?>
