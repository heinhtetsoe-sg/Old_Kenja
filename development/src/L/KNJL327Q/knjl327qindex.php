<?php

require_once('for_php7.php');

require_once('knjl327qModel.inc');
require_once('knjl327qQuery.inc');

class knjl327qController extends Controller {
    var $ModelClassName = "knjl327qModel";
    var $ProgramID      = "KNJL327Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl327qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl327qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl327qCtl = new knjl327qController;
?>
