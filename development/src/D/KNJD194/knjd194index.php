<?php

require_once('for_php7.php');

require_once('knjd194Model.inc');
require_once('knjd194Query.inc');

class knjd194Controller extends Controller {
    var $ModelClassName = "knjd194Model";
    var $ProgramID      = "KNJD194";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd194Form1");
                    }
                    break 2;
                case "":
                case "main":
                case "knjd194";
                    $sessionInstance->knjd194Model();
                    $this->callView("knjd194Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd194Ctl = new knjd194Controller;
?>
