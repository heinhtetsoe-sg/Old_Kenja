<?php

require_once('for_php7.php');

require_once('knjl334qModel.inc');
require_once('knjl334qQuery.inc');

class knjl334qController extends Controller {
    var $ModelClassName = "knjl334qModel";
    var $ProgramID      = "KNJL334Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl334q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl334qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl334qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl334qCtl = new knjl334qController;
?>
