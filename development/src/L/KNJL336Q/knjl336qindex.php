<?php

require_once('for_php7.php');

require_once('knjl336qModel.inc');
require_once('knjl336qQuery.inc');

class knjl336qController extends Controller {
    var $ModelClassName = "knjl336qModel";
    var $ProgramID      = "KNJL336Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl336q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl336qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl336qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl336qCtl = new knjl336qController;
?>
