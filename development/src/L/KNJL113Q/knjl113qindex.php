<?php

require_once('for_php7.php');

require_once('knjl113qModel.inc');
require_once('knjl113qQuery.inc');

class knjl113qController extends Controller {
    var $ModelClassName = "knjl113qModel";
    var $ProgramID      = "KNJL113Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl113q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl113qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl113qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl113qCtl = new knjl113qController;
?>
