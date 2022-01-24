<?php

require_once('for_php7.php');

require_once('knjl570aModel.inc');
require_once('knjl570aQuery.inc');

class knjl570aController extends Controller {
    var $ModelClassName = "knjl570aModel";
    var $ProgramID      = "KNJL570A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "end":
                    $this->callView("knjl570aForm1");
                    break 2;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl570aForm1");
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
$knjl570aCtl = new knjl570aController;
?>
