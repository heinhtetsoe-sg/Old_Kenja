<?php

require_once('for_php7.php');

require_once('knjd624cModel.inc');
require_once('knjd624cQuery.inc');

class knjd624cController extends Controller {
    var $ModelClassName = "knjd624cModel";
    var $ProgramID      = "KNJD624C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd624c":
                    $sessionInstance->knjd624cModel();
                    $this->callView("knjd624cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd624cCtl = new knjd624cController;
?>
