<?php

require_once('for_php7.php');

require_once('knjl044qModel.inc');
require_once('knjl044qQuery.inc');

class knjl044qController extends Controller {
    var $ModelClassName = "knjl044qModel";
    var $ProgramID      = "KNJL044Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl044q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl044qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl044qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl044qCtl = new knjl044qController;
?>
