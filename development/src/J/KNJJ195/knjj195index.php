<?php

require_once('for_php7.php');

require_once('knjj195Model.inc');
require_once('knjj195Query.inc');

class knjj195Controller extends Controller {
    var $ModelClassName = "knjj195Model";
    var $ProgramID      = "KNJJ195";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjj195Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjj195Form1");
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
$knjj195Ctl = new knjj195Controller;
?>
