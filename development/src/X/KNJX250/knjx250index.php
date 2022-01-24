<?php

require_once('for_php7.php');

require_once('knjx250Model.inc');
require_once('knjx250Query.inc');

class knjx250Controller extends Controller {
    var $ModelClassName = "knjx250Model";
    var $ProgramID      = "KNJX250";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if ($sessionInstance->makeCsvModel()){
                        $this->callView("knjx250Form1");
                    }
                    break 2;
                case "csvGet":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if ($sessionInstance->getDownloadModel()){
                        $this->callView("knjx250Form1");
                    }
                    break 2;
                case "execute":
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("knjx250");
                    break 1;
                case "del":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    if ($sessionInstance->getDeleteModel()){
                        $this->callView("knjx250Form1");
                    }
                    break 2;
                case "":
                case "knjx250":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjx250Model();
                    $this->callView("knjx250Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjx250Ctl = new knjx250Controller;
//var_dump($_REQUEST);
?>
