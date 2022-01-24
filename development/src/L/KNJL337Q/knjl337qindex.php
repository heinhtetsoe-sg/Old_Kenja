<?php

require_once('for_php7.php');

require_once('knjl337qModel.inc');
require_once('knjl337qQuery.inc');

class knjl337qController extends Controller {
    var $ModelClassName = "knjl337qModel";
    var $ProgramID      = "KNJL337Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl337q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl337qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl337qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl337qCtl = new knjl337qController;
?>
