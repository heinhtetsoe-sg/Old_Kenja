<?php

require_once('for_php7.php');

require_once('knjh530Model.inc');
require_once('knjh530Query.inc');

class knjh530Controller extends Controller {
    var $ModelClassName = "knjh530Model";
    var $ProgramID      = "KNJH530";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("knjh530");
                    break 1;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("knjh530");
                    break 1;
                case "":
                case "nendoAdd":
                case "knjh530":
                case "changeYear":
                case "back":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh530Model();
                    $this->callView("knjh530Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh530Ctl = new knjh530Controller;
?>
