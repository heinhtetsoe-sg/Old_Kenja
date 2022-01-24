<?php

require_once('for_php7.php');

require_once('knjf161Model.inc');
require_once('knjf161Query.inc');

class knjf161Controller extends Controller {
    var $ModelClassName = "knjf161Model";
    var $ProgramID      = "KNJF161";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf161":
                    $sessionInstance->knjf161Model();
                    $this->callView("knjf161Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf161Ctl = new knjf161Controller;
?>
