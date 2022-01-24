<?php

require_once('for_php7.php');

require_once('knjx251Model.inc');
require_once('knjx251Query.inc');

class knjx251Controller extends Controller {
    var $ModelClassName = "knjx251Model";
    var $ProgramID      = "KNJX251";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if ($sessionInstance->makeCsvModel()){
                        $this->callView("knjx251Form1");
                    }
                    break 2;
                case "csvGet":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if ($sessionInstance->getDownloadModel()){
                        $this->callView("knjx251Form1");
                    }
                    break 2;
                case "execute":
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("knjx251");
                    break 1;
                case "del":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    if ($sessionInstance->getDeleteModel()){
                        $this->callView("knjx251Form1");
                    }
                    break 2;
                case "":
                case "knjx251":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjx251Model();
                    $this->callView("knjx251Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjx251Ctl = new knjx251Controller;
//var_dump($_REQUEST);
?>
