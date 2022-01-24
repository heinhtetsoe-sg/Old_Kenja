<?php

require_once('for_php7.php');

require_once('knjd672Model.inc');
require_once('knjd672Query.inc');

class knjd672Controller extends Controller {
    var $ModelClassName = "knjd672Model";
    var $ProgramID      = "KNJD672";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd672":
                    $sessionInstance->knjd672Model();
                    $this->callView("knjd672Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd672Ctl = new knjd672Controller;
?>
