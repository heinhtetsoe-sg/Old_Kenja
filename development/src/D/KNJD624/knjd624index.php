<?php

require_once('for_php7.php');

require_once('knjd624Model.inc');
require_once('knjd624Query.inc');

class knjd624Controller extends Controller {
    var $ModelClassName = "knjd624Model";
    var $ProgramID      = "KNJD624";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd624":
                    $sessionInstance->knjd624Model();
                    $this->callView("knjd624Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd624Ctl = new knjd624Controller;
var_dump($_REQUEST);
?>
