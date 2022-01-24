<?php

require_once('for_php7.php');

require_once('knjl312pModel.inc');
require_once('knjl312pQuery.inc');

class knjl312pController extends Controller {
    var $ModelClassName = "knjl312pModel";
    var $ProgramID      = "KNJL312P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl312p":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl312pModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl312pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl312pCtl = new knjl312pController;
?>
