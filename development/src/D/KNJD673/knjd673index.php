<?php

require_once('for_php7.php');

require_once('knjd673Model.inc');
require_once('knjd673Query.inc');

class knjd673Controller extends Controller {
    var $ModelClassName = "knjd673Model";
    var $ProgramID      = "KNJD673";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd673":
                    $sessionInstance->knjd673Model();
                    $this->callView("knjd673Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd673Ctl = new knjd673Controller;
?>
