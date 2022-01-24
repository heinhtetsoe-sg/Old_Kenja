<?php

require_once('for_php7.php');

require_once('knjx_hexam_remarkModel.inc');
require_once('knjx_hexam_remarkQuery.inc');

class knjx_hexam_remarkController extends Controller {
    var $ModelClassName = "knjx_hexam_remarkModel";
    var $ProgramID      = "KNJX_HEXAM_REMARK";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjx_hexam_remarkForm1"); //備考一括更新画面
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "", $sessionInstance->sendAuth);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->updateModel();
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
$knjx_hexam_remarkCtl = new knjx_hexam_remarkController;
?>
