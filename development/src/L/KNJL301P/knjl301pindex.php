<?php

require_once('for_php7.php');

require_once('knjl301pModel.inc');
require_once('knjl301pQuery.inc');

class knjl301pController extends Controller {
    var $ModelClassName = "knjl301pModel";
    var $ProgramID      = "KNJL301P";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl301p":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl301pModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl301pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl301pCtl = new knjl301pController;
?>
