<?php

require_once('for_php7.php');


require_once('knjd128lModel.inc');
require_once('knjd128lQuery.inc');

class knjd128lController extends Controller {
    var $ModelClassName = "knjd128lModel";
    var $ProgramID      = "KNJD128L";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "subclasscd":
                    $this->callView("knjd128lForm1");
                    break 2;
                case "chaircd":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd128lForm1");
                    break 2;
                case "calc":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd128lForm1");
                    break 2;
                case "update":
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd128lForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd128lCtl = new knjd128lController;
?>
