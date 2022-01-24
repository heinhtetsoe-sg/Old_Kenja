<?php

require_once('for_php7.php');

require_once('knjf163Model.inc');
require_once('knjf163Query.inc');

class knjf163Controller extends Controller {
    var $ModelClassName = "knjf163Model";
    var $ProgramID      = "KNJF163";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjf163Form1");
                    }
                    break 2;
                case "":
                case "knjf163":
                    $sessionInstance->knjf163Model();
                    $this->callView("knjf163Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf163Ctl = new knjf163Controller;
//var_dump($_REQUEST);
?>
