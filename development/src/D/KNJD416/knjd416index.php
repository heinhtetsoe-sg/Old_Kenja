<?php

require_once('for_php7.php');

require_once('knjd416Model.inc');
require_once('knjd416Query.inc');

class knjd416Controller extends Controller {
    var $ModelClassName = "knjd416Model";
    var $ProgramID      = "KNJD416";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "gakunen":
                case "kojin":
                case "back":
                case "copy_after":
                    $this->callView("knjd416Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("copy_after");
                    break 1;            
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd416Ctl = new knjd416Controller;
?>
