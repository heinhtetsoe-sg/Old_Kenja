<?php
require_once('knjo112Model.inc');
require_once('knjo112Query.inc');

class knjo112Controller extends Controller {
    var $ModelClassName = "knjo112Model";
    var $ProgramID      = "KNJO112";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec": 
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjo112Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "read": 
                    if(!$sessionInstance->updateModel()){
                        $this->callView("knjo112Form1");
                    }
                    break 2;
                case "":
                case "main":
                case "change":
                    $this->callView("knjo112Form1");
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
$knjo112Ctl = new knjo112Controller;
?>
