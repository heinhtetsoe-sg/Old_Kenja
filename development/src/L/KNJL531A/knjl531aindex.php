<?php

require_once('for_php7.php');

require_once('knjl531aModel.inc');
require_once('knjl531aQuery.inc');

class knjl531aController extends Controller {
    var $ModelClassName = "knjl531aModel";
    var $ProgramID      = "KNJL531A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "end":
                    $this->callView("knjl531aForm1");
                    break 2;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl531aForm1");
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
$knjl531aCtl = new knjl531aController;
?>
