<?php

require_once('for_php7.php');

require_once('knjh312Model.inc');
require_once('knjh312Query.inc');

class knjh312Controller extends Controller {
    var $ModelClassName = "knjh312Model";
    var $ProgramID      = "KNJH312";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "yearChange":
                    $this->callView("knjh312Form1");
                    exit;
                case "":
                    $sessionInstance->knjh312Model();
                    $this->callView("knjh312Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh312Ctl = new knjh312Controller;
?>
