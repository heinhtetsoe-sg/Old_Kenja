<?php

require_once('for_php7.php');

require_once('knjxattend_htrainremark_detailModel.inc');
require_once('knjxattend_htrainremark_detailQuery.inc');

class knjxattend_htrainremark_detailController extends Controller {
    var $ModelClassName = "knjxattend_htrainremark_detailModel";
    var $ProgramID      = "knjxattend_htrainremark_detail";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjxattend_htrainremark_detailForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxattend_htrainremark_detailCtl = new knjxattend_htrainremark_detailController;
?>
