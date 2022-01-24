<?php

require_once('for_php7.php');

require_once('knje360aModel.inc');
require_once('knje360aQuery.inc');

class knje360aController extends Controller {
    var $ModelClassName = "knje360aModel";
    var $ProgramID      = "KNJE360A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "sort":
                case "reset":
                    $this->callView("knje360aForm1");
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "select":  //学校選択
                case "select_search":
                case "select_school":
                    $this->callView("knje360aSubform1");
                    break 2;
                case "replace": //一括変更
                    $this->callView("knje360aSubform2");
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
$knje360aCtl = new knje360aController;
?>
