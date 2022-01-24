<?php

require_once('for_php7.php');

require_once('knjl345qModel.inc');
require_once('knjl345qQuery.inc');

class knjl345qController extends Controller {
    var $ModelClassName = "knjl345qModel";
    var $ProgramID      = "KNJL345Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl345q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl345qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl345qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl345qCtl = new knjl345qController;
?>
