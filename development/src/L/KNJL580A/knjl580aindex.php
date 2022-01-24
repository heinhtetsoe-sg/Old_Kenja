<?php

require_once('for_php7.php');

require_once('knjl580aModel.inc');
require_once('knjl580aQuery.inc');

class knjl580aController extends Controller {
    var $ModelClassName = "knjl580aModel";
    var $ProgramID      = "KNJL580A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "end":
                    $this->callView("knjl580aForm1");
                    break 2;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl580aForm1");
                    }
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl580aCtl = new knjl580aController;
?>
