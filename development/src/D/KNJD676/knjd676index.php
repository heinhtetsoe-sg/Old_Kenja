<?php

require_once('for_php7.php');

require_once('knjd676Model.inc');
require_once('knjd676Query.inc');

class knjd676Controller extends Controller {
    var $ModelClassName = "knjd676Model";
    var $ProgramID      = "KNJD676";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd676":
                    $sessionInstance->knjd676Model();
                    $this->callView("knjd676Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd676Ctl = new knjd676Controller;
?>
