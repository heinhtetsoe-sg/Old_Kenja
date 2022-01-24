<?php

require_once('for_php7.php');


require_once('knje372eModel.inc');
require_once('knje372eQuery.inc');

class knje372eController extends Controller {
    var $ModelClassName = "knje372eModel";
    var $ProgramID      = "KNJE372E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "check":
                case "reset":
                    $this->callView("knje372eForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    break 1;
                case "csv":
                case "csvComp":
                    $this->callView("knje372eSubForm1");
                    break 2;
                case "csvExec";     //CSV処理 取込
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->setCmd("csv");
                    if ($sessionInstance->getCsvExecModel()) {
                        $sessionInstance->setCmd("csvComp");
                    }
                    break 1;
                case "csvDownload"; //CSV処理 データ出力
                    if(!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje372eSubForm1");
                    }
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
$knje372eCtl = new knje372eController;
?>
