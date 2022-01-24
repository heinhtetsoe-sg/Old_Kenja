<?php

require_once('for_php7.php');

require_once('knjh441bModel.inc');
require_once('knjh441bQuery.inc');

class knjh441bController extends Controller {
    var $ModelClassName = "knjh441bModel";
    var $ProgramID      = "KNJH441B";

    function main() {

        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "init":
                case "changeYear":
                case "knjh441b":
                    $sessionInstance->knjh441bModel();
                    $this->callView("knjh441bForm1");
                    break 2;

                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjh441bForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;

                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh441bCtl = new knjh441bController;
?>
