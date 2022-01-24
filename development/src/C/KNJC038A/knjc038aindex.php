<?php

require_once('for_php7.php');

require_once('knjc038aModel.inc');
require_once('knjc038aQuery.inc');

class knjc038aController extends Controller {
    var $ModelClassName = "knjc038aModel";
    var $ProgramID      = "KNJC038A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":            //データ取込
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID); 
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv_error":       //エラー出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjc038aForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID); 
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjc038aForm1");
                    break 2;
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
$knjc038aCtl = new knjc038aController;
?>
