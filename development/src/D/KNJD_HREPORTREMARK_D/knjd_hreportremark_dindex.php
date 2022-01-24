<?php

require_once('for_php7.php');

require_once('knjd_hreportremark_dModel.inc');
require_once('knjd_hreportremark_dQuery.inc');

class knjd_hreportremark_dController extends Controller {
    var $ModelClassName = "knjd_hreportremark_dModel";
    var $ProgramID      = "KNJD_HREPORTREMARK_D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("form1");
                    break 1;
                case "form1":
                case "edit":
                case "clear":
                    $this->callView("knjd_hreportremark_dForm1");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knjd_hreportremark_dModel();
                    $this->callView("knjd_hreportremark_dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd_hreportremark_dCtl = new knjd_hreportremark_dController;
?>
