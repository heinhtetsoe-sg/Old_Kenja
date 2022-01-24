<?php

require_once('for_php7.php');

require_once('knjd128xModel.inc');
require_once('knjd128xQuery.inc');

class knjd128xController extends Controller {
    var $ModelClassName = "knjd128xModel";
    var $ProgramID      = "KNJD128X";

    function main()
    {
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
                        $this->callView("knjd128xForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID); 
                    break 2;
                case "":
                case "changeKind":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjd128xForm1");
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
$knjd128xCtl = new knjd128xController;
?>
