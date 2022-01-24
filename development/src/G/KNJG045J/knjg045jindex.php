<?php

require_once('for_php7.php');

require_once('knjg045jModel.inc');
require_once('knjg045jQuery.inc');

class knjg045jController extends Controller {
    var $ModelClassName = "knjg045jModel";
    var $ProgramID      = "KNJG045J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                case "delete":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "lesson":
                case "read1":
                case "read2":
                case "main":
                    $this->callView("knjg045jForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjg045jForm1");
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
$knjg045jCtl = new knjg045jController;
//var_dump($_REQUEST);
?>
