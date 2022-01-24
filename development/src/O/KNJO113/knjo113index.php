<?php
require_once('knjo113Model.inc');
require_once('knjo113Query.inc');

class knjo113Controller extends Controller {
    var $ModelClassName = "knjo113Model";
    var $ProgramID      = "KNJO113";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec": 
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjo113Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "read": 
                    if(!$sessionInstance->updateModel()){
                        $this->callView("knjo113Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjo113Form1");
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
$knjo113Ctl = new knjo113Controller;
?>
