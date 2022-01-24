<?php

require_once('for_php7.php');

require_once('knjf306aModel.inc');
require_once('knjf306aQuery.inc');

class knjf306aController extends Controller {
    var $ModelClassName = "knjf306aModel";
    var $ProgramID      = "KNJF306A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf306a":
                    $sessionInstance->knjf306aModel();
                    $this->callView("knjf306aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjf306aForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf306aCtl = new knjf306aController;
?>
