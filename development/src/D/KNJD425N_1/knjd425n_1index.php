<?php

require_once('for_php7.php');

require_once('knjd425n_1Model.inc');
require_once('knjd425n_1Query.inc');

class knjd425n_1Controller extends Controller {
    var $ModelClassName = "knjd425n_1Model";
    var $ProgramID      = "KNJD425N_1";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "subform":
                case "changeSemester":
                case "sort":
                case "updateEnd":
                    $sessionInstance->knjd425n_1Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd425n_1Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updateEnd");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("updateEnd");
                    break 1;
                case "ziritu":
                case "changeTarget":
                case "zirituInsertEnd":
                    $this->callView("knjd425n_1Ziritu");
                    break 2;
                case "zirituInsert":
                    $sessionInstance->getZirituInsertModel();
                    $sessionInstance->setCmd("zirituInsertEnd");
                    break 1;
                case "lastYearData1":
                    $this->callView("knjd425n_1LastYearData1");
                    break 2;
                case "lastYearData2":
                    $this->callView("knjd425n_1LastYearData2");
                    break 2;
                case "lastYearData3":
                    $this->callView("knjd425n_1LastYearData3");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd425n_1Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd425n_1Ctl = new knjd425n_1Controller;
?>
