<?php

require_once('for_php7.php');

require_once('knjx655ccsvModel.inc');
require_once('knjx655ccsvQuery.inc');

class knjx655ccsvController extends Controller {
    var $ModelClassName = "knjx655ccsvModel";
    var $ProgramID      = "KNJX655CCSV";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":       //CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjx655ccsvForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjx655ccsvForm1");
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
$knjx655ccsvCtl = new knjx655ccsvController;
?>
