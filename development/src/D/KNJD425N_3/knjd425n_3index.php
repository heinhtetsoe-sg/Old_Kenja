<?php

require_once('for_php7.php');

require_once('knjd425n_3Model.inc');
require_once('knjd425n_3Query.inc');

class knjd425n_3Controller extends Controller {
    var $ModelClassName = "knjd425n_3Model";
    var $ProgramID      = "KNJD425N_3";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "subform":
                case "changeSemester":
                case "changeRadio":
                case "changeSubclasscd":
                case "updateEnd":
                case "reset":
                case "sort":
                    $sessionInstance->knjd425n_3Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd425n_3Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updateEnd");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("updateEnd");
                    break 1;
                case "listdelete":
                    $sessionInstance->getListDeleteModel();
                    $sessionInstance->setCmd("updateEnd");
                    break 1;
                case "targetClass":
                case "changeTargetClassSubclasscd":
                case "targetClassInsertEnd":
                    $this->callView("knjd425n_3TargetClass");
                    break 2;
                case "targetClassInsert":
                    $sessionInstance->getTargetClassInsertModel();
                    $sessionInstance->setCmd("targetClassInsertEnd");
                    break 1;
                case "gouri":
                    $this->callView("knjd425n_3Gouri");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd425n_3Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd425n_3Ctl = new knjd425n_3Controller;
?>
