<?php

require_once('for_php7.php');

require_once('knjx_c031fModel.inc');
require_once('knjx_c031fQuery.inc');

class knjx_c031fController extends Controller {
    var $ModelClassName = "knjx_c031fModel";
    var $ProgramID      = "KNJX_C031F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID); 
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjx_c031fForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID); 
                    break 2;
                case "":
                case "main":
                case "change_radio":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx_c031fForm1");
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
$knjx_c031fCtl = new knjx_c031fController;
?>
