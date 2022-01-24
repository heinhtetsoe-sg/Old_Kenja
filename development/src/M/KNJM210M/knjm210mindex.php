<?php

require_once('for_php7.php');

require_once('knjm210mModel.inc');
require_once('knjm210mQuery.inc');

class knjm210mController extends Controller {
    var $ModelClassName = "knjm210mModel";
    var $ProgramID      = "KNJM210M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $this->callView("knjm210mForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm210mCtl = new knjm210mController;
//var_dump($_REQUEST);
?>
