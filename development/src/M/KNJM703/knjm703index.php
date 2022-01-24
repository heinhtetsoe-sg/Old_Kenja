<?php

require_once('for_php7.php');

require_once('knjm703Model.inc');
require_once('knjm703Query.inc');

class knjm703Controller extends Controller {
    var $ModelClassName = "knjm703Model";
    var $ProgramID      = "KNJM703";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjm703";
                    $sessionInstance->knjm703Model();
                    $this->callView("knjm703Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm703Ctl = new knjm703Controller;
?>
