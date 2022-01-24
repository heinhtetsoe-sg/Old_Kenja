<?php
require_once('knjo101Model.inc');
require_once('knjo101Query.inc');
require_once(DOCUMENTROOT.'/common/importXml.php');

class knjo101Controller extends Controller {
    var $ModelClassName = "knjo101Model";
    var $ProgramID      = "KNJO101";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec": 
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjo101Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "read": 
                    if($sessionInstance->fileUpdate()){
                        $sessionInstance->updateModel();
                        $sessionInstance->insertModel();
                    }
                    $this->callView("knjo101Form1");
                    break 2;
                case "delete": 
                    if($sessionInstance->fileUpdate()){
                        $sessionInstance->deleteModel();
                        if($sessionInstance->updateModel()){
                            $sessionInstance->insertModel();
                        }
                    }
                    $this->callView("knjo101Form1");
                    break 2;
                case "":
                case "main":
                    $this->callView("knjo101Form1");
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
$knjo101Ctl = new knjo101Controller;
?>
