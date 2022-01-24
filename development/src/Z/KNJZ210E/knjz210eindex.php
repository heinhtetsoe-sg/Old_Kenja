<?php

require_once('for_php7.php');

require_once('knjz210eModel.inc');
require_once('knjz210eQuery.inc');

class knjz210eController extends Controller {
    var $ModelClassName = "knjz210eModel";
    var $ProgramID      = "KNJZ210E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "kakutei":
                case "main":
                case "reset":
                    $this->callView("knjz210eForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("kakutei");
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
$knjz210eCtl = new knjz210eController;
?>
