<?php

require_once('for_php7.php');

require_once('knjd671Model.inc');
require_once('knjd671Query.inc');

class knjd671Controller extends Controller {
    var $ModelClassName = "knjd671Model";
    var $ProgramID      = "KNJD671";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd671":
                    $sessionInstance->knjd671Model();
                    $this->callView("knjd671Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd671Ctl = new knjd671Controller;
?>
