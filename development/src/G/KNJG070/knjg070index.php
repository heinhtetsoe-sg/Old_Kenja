<?php

require_once('for_php7.php');

require_once('knjg070Model.inc');
require_once('knjg070Query.inc');

class knjg070Controller extends Controller {
    var $ModelClassName = "knjg070Model";
    var $ProgramID      = "KNJG070";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knjg070";
                case "search";
                    $sessionInstance->knjg070Model();
                    $this->callView("knjg070Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg070Ctl = new knjg070Controller;
?>
