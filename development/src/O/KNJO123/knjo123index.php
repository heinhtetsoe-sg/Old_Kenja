<?php
require_once('knjo123Model.inc');
require_once('knjo123Query.inc');

class knjo123Controller extends Controller {
    var $ModelClassName = "knjo123Model";
    var $ProgramID      = "KNJo123";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "search":
                    $sessionInstance->schregChk($sessionInstance->SCHREGNO);
                    $this->callView("knjo123Form1");
                    exit;
                case "exec": 
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjo123Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "read": 
                    if(!$sessionInstance->updateModel()){
                        $this->callView("knjo123Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjo123Form1");
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
$knjo123Ctl = new knjo123Controller;
?>
