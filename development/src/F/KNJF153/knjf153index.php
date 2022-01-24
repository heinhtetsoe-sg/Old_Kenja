<?php

require_once('for_php7.php');

require_once('knjf153Model.inc');
require_once('knjf153Query.inc');

class knjf153Controller extends Controller {
    var $ModelClassName = "knjf153Model";
    var $ProgramID      = "KNJF153";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf153":
                    $sessionInstance->knjf153Model();
                    $this->callView("knjf153Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjf153Form1");
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
$knjf153Ctl = new knjf153Controller;
//var_dump($_REQUEST);
?>
