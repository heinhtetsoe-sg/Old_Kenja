<?php

require_once('for_php7.php');

require_once('knjp726Model.inc');
require_once('knjp726Query.inc');

class knjp726Controller extends Controller {
    var $ModelClassName = "knjp726Model";
    var $ProgramID      = "KNJP726";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //CSV取込
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                //CSV出力
                case "head":
                    if (!$sessionInstance->OutputDataFileHead()) {
                        $this->callView("knjp726Form1");
                    }
                    break 2;
                case "error":
                    if (!$sessionInstance->OutputDataFileError()) {
                        $this->callView("knjp726Form1");
                    }
                    break 2;
                case "data":
                    if (!$sessionInstance->OutputDataFile()) {
                        $this->callView("knjp726Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjp726Form1");
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
$knjp726Ctl = new knjp726Controller;
?>
