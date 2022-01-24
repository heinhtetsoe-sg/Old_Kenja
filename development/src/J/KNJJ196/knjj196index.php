<?php

require_once('for_php7.php');

require_once('knjj196Model.inc');
require_once('knjj196Query.inc');

class knjj196Controller extends Controller {
    var $ModelClassName = "knjj196Model";
    var $ProgramID      = "KNJJ196";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjj196Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjj196Form1");
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
$knjj196Ctl = new knjj196Controller;
?>
