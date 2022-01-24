<?php
require_once('knjo133Model.inc');
require_once('knjo133Query.inc');

class knjo133Controller extends Controller {
    var $ModelClassName = "knjo133Model";
    var $ProgramID      = "KNJO133";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "search":
                    $sessionInstance->schregChk($sessionInstance->SCHREGNO);
                    $this->callView("knjo133Form1");
                    exit;
                case "exec": 
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjo133Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "read": 
                    if(!$sessionInstance->updateModel()){
                        $this->callView("knjo133Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjo133Form1");
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
$knjo133Ctl = new knjo133Controller;
?>
