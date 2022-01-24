<?php
require_once('knjo132Model.inc');
require_once('knjo132Query.inc');

class knjo132Controller extends Controller {
    var $ModelClassName = "knjo132Model";
    var $ProgramID      = "KNJO132";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "search":
                    $sessionInstance->schregChk($sessionInstance->SCHREGNO);
                    $this->callView("knjo132Form1");
                    exit;
                case "exec": 
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjo132Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "read": 
                    if(!$sessionInstance->updateModel()){
                        $this->callView("knjo132Form1");
                    }
                    break 2;
                case "":
                case "main":
                case "change":
                    $this->callView("knjo132Form1");
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
$knjo132Ctl = new knjo132Controller;
?>
