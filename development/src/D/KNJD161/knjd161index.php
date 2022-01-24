<?php

require_once('for_php7.php');

require_once('knjd161Model.inc');
require_once('knjd161Query.inc');

class knjd161Controller extends Controller {
    var $ModelClassName = "knjd161Model";
    var $ProgramID      = "KNJD161";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd161":
                    $sessionInstance->knjd161Model();
                    $this->callView("knjd161Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd161Ctl = new knjd161Controller;
var_dump($_REQUEST);
?>
