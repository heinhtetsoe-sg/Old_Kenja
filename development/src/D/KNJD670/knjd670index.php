<?php

require_once('for_php7.php');

require_once('knjd670Model.inc');
require_once('knjd670Query.inc');

class knjd670Controller extends Controller {
    var $ModelClassName = "knjd670Model";
    var $ProgramID      = "KNJD670";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd670":
                    $sessionInstance->knjd670Model();
                    $this->callView("knjd670Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd670Ctl = new knjd670Controller;
?>
