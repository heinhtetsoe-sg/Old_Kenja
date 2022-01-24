<?php

require_once('for_php7.php');

require_once('knjd674Model.inc');
require_once('knjd674Query.inc');

class knjd674Controller extends Controller {
    var $ModelClassName = "knjd674Model";
    var $ProgramID      = "KNJD674";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd674":
                    $sessionInstance->knjd674Model();
                    $this->callView("knjd674Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd674Ctl = new knjd674Controller;
?>
