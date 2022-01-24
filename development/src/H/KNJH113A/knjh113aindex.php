<?php

require_once('for_php7.php');

require_once('knjh113aModel.inc');
require_once('knjh113aQuery.inc');

class knjh113aController extends Controller {
    var $ModelClassName = "knjh113aModel";
    var $ProgramID      = "KNJH113A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh113a":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh113aModel();
                    $this->callView("knjh113aForm1");
                    exit;
                case "csv":         //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjh113aForm1");
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
$knjh113aCtl = new knjh113aController;
//var_dump($_REQUEST);
?>
