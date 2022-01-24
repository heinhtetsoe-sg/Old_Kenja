<?php

require_once('for_php7.php');

require_once('knjx_rishu_kojinModel.inc');
require_once('knjx_rishu_kojinQuery.inc');

class knjx_rishu_kojinController extends Controller {
    var $ModelClassName = "knjx_rishu_kojinModel";
    var $ProgramID      = "KNJX_RISHU_KOJIN";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "check":
                case "edit":
                case "main":
                case "clear":
                    $this->callView("knjx_rishu_kojinForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_rishu_kojinCtl = new knjx_rishu_kojinController;
?>
