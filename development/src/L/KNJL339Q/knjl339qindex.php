<?php

require_once('for_php7.php');

require_once('knjl339qModel.inc');
require_once('knjl339qQuery.inc');

class knjl339qController extends Controller {
    var $ModelClassName = "knjl339qModel";
    var $ProgramID      = "KNJL339Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl339q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl339qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl339qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl339qCtl = new knjl339qController;
?>
