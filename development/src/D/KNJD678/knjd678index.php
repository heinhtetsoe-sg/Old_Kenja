<?php

require_once('for_php7.php');

require_once('knjd678Model.inc');
require_once('knjd678Query.inc');

class knjd678Controller extends Controller {
    var $ModelClassName = "knjd678Model";
    var $ProgramID      = "KNJD678";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd678":
                    $sessionInstance->knjd678Model();
                    $this->callView("knjd678Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd678Ctl = new knjd678Controller;
?>
