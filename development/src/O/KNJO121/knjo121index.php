<?php
require_once('knjo121Model.inc');
require_once('knjo121Query.inc');
require_once(DOCUMENTROOT.'/common/importXml.php');

class knjo121Controller extends Controller {
    var $ModelClassName = "knjo121Model";
    var $ProgramID      = "KNJO121";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec": 
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjo121Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "read": 
                    if($sessionInstance->fileUpdate() && $sessionInstance->validateCode()){
                        $sessionInstance->updateModel();
                        $sessionInstance->insertModel();
                    }
                    $this->callView("knjo121Form1");
                    break 2;
                case "delete": 
                    if($sessionInstance->fileUpdate() && $sessionInstance->validateCode()){
                        $sessionInstance->deleteModel();
                        if($sessionInstance->updateModel()){
                            $sessionInstance->insertModel();
                        }
                    }
                    $this->callView("knjo121Form1");
                    break 2;
                case "":
                case "main":
                    $this->callView("knjo121Form1");
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
$knjo121Ctl = new knjo121Controller;
?>
