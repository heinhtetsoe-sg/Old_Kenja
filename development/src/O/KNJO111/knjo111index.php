<?php
require_once('knjo111Model.inc');
require_once('knjo111Query.inc');
require_once(DOCUMENTROOT.'/common/importXml.php');

class knjo111Controller extends Controller {
    var $ModelClassName = "knjo111Model";
    var $ProgramID      = "KNJO111";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec": 
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjo111Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "read": 
                    if($sessionInstance->fileUpdate()){
                        $sessionInstance->updateModel();
                        $sessionInstance->insertModel();
                    }
                    $this->callView("knjo111Form1");
                    break 2;
                case "delete": 
                    if($sessionInstance->fileUpdate()){
                        $sessionInstance->deleteModel();
                        if($sessionInstance->updateModel()){
                            $sessionInstance->insertModel();
                        }
                    }
                    $this->callView("knjo111Form1");
                    break 2;
                case "":
                case "main":
                    $this->callView("knjo111Form1");
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
$knjo111Ctl = new knjo111Controller;
?>
