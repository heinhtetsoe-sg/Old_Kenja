<?php

require_once('for_php7.php');

require_once('knjz211fModel.inc');
require_once('knjz211fQuery.inc');

class knjz211fController extends Controller {
    var $ModelClassName = "knjz211fModel";
    var $ProgramID      = "KNJZ211F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "copy_kakutei":
                case "kakutei":
                case "change":
                case "main":
                case "reset":
                    $this->callView("knjz211fForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("kakutei");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("kakutei");
                    break 1;
                case "update2_yes":
                    $sessionInstance->getUpdateYesModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("kakutei");
                    break 1;
                case "update2_no":
                    $sessionInstance->getUpdateNoModel();
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
$knjz211fCtl = new knjz211fController;
?>
