<?php

require_once('for_php7.php');

require_once('knjd_hreportremark_d_2Model.inc');
require_once('knjd_hreportremark_d_2Query.inc');

class knjd_hreportremark_d_2Controller extends Controller {
    var $ModelClassName = "knjd_hreportremark_d_2Model";
    var $ProgramID      = "KNJD_HREPORTREMARK_D_2";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit2");
                    break 1;
                case "form1":
                case "edit":
                case "updEdit2":
                case "clear":
                    $this->callView("knjd_hreportremark_d_2Form1");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knjd_hreportremark_d_2Model();
                    $this->callView("knjd_hreportremark_d_2Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd_hreportremark_d_2Ctl = new knjd_hreportremark_d_2Controller;
?>
