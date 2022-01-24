<?php

require_once('for_php7.php');

require_once('knjd665Model.inc');
require_once('knjd665Query.inc');

class knjd665Controller extends Controller {
    var $ModelClassName = "knjd665Model";
    var $ProgramID      = "KNJD665";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd665":
                    $sessionInstance->knjd665Model();
                    $this->callView("knjd665Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd665Ctl = new knjd665Controller;
?>
