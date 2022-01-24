<?php
require_once('knjo102Model.inc');
require_once('knjo102Query.inc');

class knjo102Controller extends Controller {
    var $ModelClassName = "knjo102Model";
    var $ProgramID      = "KNJO102";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec": 
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjo102Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "read": 
                    if(!$sessionInstance->updateModel()){
                        $this->callView("knjo102Form1");
                    }
                    break 2;
                case "":
                case "main":
                case "change":
                    $this->callView("knjo102Form1");
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
$knjo102Ctl = new knjo102Controller;
?>
