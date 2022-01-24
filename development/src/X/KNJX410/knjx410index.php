<?php

require_once('for_php7.php');

require_once('knjx410Model.inc');
require_once('knjx410Query.inc');

class knjx410Controller extends Controller {
    var $ModelClassName = "knjx410Model";
    var $ProgramID      = "KNJX410";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knjx410";
                case "KNJX400S";
                    $sessionInstance->knjx410Model();
                    $this->callView("knjx410Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx410Ctl = new knjx410Controller;
?>
