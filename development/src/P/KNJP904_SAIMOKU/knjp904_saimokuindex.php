<?php

require_once('for_php7.php');

require_once('knjp904_saimokuModel.inc');
require_once('knjp904_saimokuQuery.inc');

class knjp904_saimokuController extends Controller {
    var $ModelClassName = "knjp904_saimokuModel";
    var $ProgramID      = "KNJP904_SAIMOKU";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjp904_saimoku":
                case "reset":
                    $sessionInstance->knjp904_saimokuModel();       //コントロールマスタの呼び出し
                    $this->callView("knjp904_saimokuForm1");
                    exit;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp904_saimokuCtl = new knjp904_saimokuController;
?>
