<?php
require_once('knjo103Model.inc');
require_once('knjo103Query.inc');

class knjo103Controller extends Controller {
    var $ModelClassName = "knjo103Model";
    var $ProgramID      = "KNJO103";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec": 
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjo103Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "read": 
                    if(!$sessionInstance->updateModel()){
                        $this->callView("knjo103Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjo103Form1");
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
$knjo103Ctl = new knjo103Controller;
?>
