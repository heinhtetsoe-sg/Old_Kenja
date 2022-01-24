<?php

require_once('for_php7.php');

require_once('knjl045qModel.inc');
require_once('knjl045qQuery.inc');

class knjl045qController extends Controller {
    var $ModelClassName = "knjl045qModel";
    var $ProgramID      = "KNJL045Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl045q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl045qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl045qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl045qCtl = new knjl045qController;
?>
