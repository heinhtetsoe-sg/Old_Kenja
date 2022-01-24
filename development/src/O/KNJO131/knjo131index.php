<?php
require_once('knjo131Model.inc');
require_once('knjo131Query.inc');
require_once(DOCUMENTROOT.'/common/importXml.php');

class knjo131Controller extends Controller {
    var $ModelClassName = "knjo131Model";
    var $ProgramID      = "KNJO131";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "search":
                    $sessionInstance->schregChk($sessionInstance->SCHREGNO);
                    $this->callView("knjo131Form1");
                    exit;
                case "exec": 
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjo131Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "read": 
                    if($sessionInstance->fileUpdate() && $sessionInstance->validateCode()){
                        $sessionInstance->updateModel();
                        $sessionInstance->insertModel();
                    }
                    $this->callView("knjo131Form1");
                    break 2;
                case "delete": 
                    if($sessionInstance->fileUpdate() && $sessionInstance->validateCode()){
                        $sessionInstance->deleteModel();
                        if($sessionInstance->updateModel()){
                            $sessionInstance->insertModel();
                        }
                    }
                    $this->callView("knjo131Form1");
                    break 2;
                case "":
                case "main":
                    $this->callView("knjo131Form1");
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
$knjo131Ctl = new knjo131Controller;
?>
