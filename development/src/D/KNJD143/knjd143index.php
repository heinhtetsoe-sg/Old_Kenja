<?php

require_once('for_php7.php');


require_once('knjd143Model.inc');
require_once('knjd143Query.inc');

class knjd143Controller extends Controller {
    var $ModelClassName = "knjd143Model";
    var $ProgramID      = "KNJD143";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "csvInputMain":
                case "subclasscd":
                case "reset":
                case "value_set":
                case "back":
                case "semester":
                    $this->callView("knjd143Form1");
                    break 2;
                case "chaircd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjd143Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
//                case "csvInput":    //CSV取込
//                    $sessionInstance->setAccessLogDetail("EI", $ProgramID); 
//                    $sessionInstance->getCsvInputModel();
//                    $sessionInstance->setCmd("csvInputMain");
//                    break 1;
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
$knjd143Ctl = new knjd143Controller;
?>
