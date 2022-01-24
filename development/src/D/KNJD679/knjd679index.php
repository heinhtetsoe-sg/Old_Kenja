<?php

require_once('for_php7.php');

require_once('knjd679Model.inc');
require_once('knjd679Query.inc');

class knjd679Controller extends Controller {
    var $ModelClassName = "knjd679Model";
    var $ProgramID      = "KNJD679";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd679":
                    $sessionInstance->knjd679Model();
                    $this->callView("knjd679Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd679Ctl = new knjd679Controller;
?>
