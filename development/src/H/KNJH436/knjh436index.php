<?php

require_once('for_php7.php');

require_once('knjh436Model.inc');
require_once('knjh436Query.inc');

class knjh436Controller extends Controller {
    var $ModelClassName = "knjh436Model";
    var $ProgramID      = "KNJH436";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":        //CSV取込
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":         //CSV出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjh436Form1");
                    }
                    break 2;
                case "":
                case "main":
                case "changeData":
                case "changeCompany":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjh436Form1");
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
$knjh436Ctl = new knjh436Controller;
?>
