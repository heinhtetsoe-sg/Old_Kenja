<?php

require_once('for_php7.php');

require_once('knjz211aModel.inc');
require_once('knjz211aQuery.inc');

class knjz211aController extends Controller {
    var $ModelClassName = "knjz211aModel";
    var $ProgramID      = "KNJZ211A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "copy_kakutei":
                case "kakutei":
                case "change":
                case "main":
                case "reset":
                    $this->callView("knjz211aForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("kakutei");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("copy_kakutei");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
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
$knjz211aCtl = new knjz211aController;
?>
