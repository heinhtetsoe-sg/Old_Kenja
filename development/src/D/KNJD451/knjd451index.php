<?php

require_once('for_php7.php');

require_once('knjd451Model.inc');
require_once('knjd451Query.inc');

class knjd451Controller extends Controller {
    var $ModelClassName = "knjd451Model";
    var $ProgramID      = "KNJD451";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd451":
                    $sessionInstance->knjd451Model();
                    $this->callView("knjd451Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd451Ctl = new knjd451Controller;
?>
