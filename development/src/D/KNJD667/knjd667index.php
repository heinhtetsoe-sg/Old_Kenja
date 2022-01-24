<?php

require_once('for_php7.php');

require_once('knjd667Model.inc');
require_once('knjd667Query.inc');

class knjd667Controller extends Controller {
    var $ModelClassName = "knjd667Model";
    var $ProgramID      = "KNJD667";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd667":
                    $sessionInstance->knjd667Model();
                    $this->callView("knjd667Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd667Ctl = new knjd667Controller;
?>
