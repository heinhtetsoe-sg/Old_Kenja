<?php

require_once('for_php7.php');

require_once('knjz210nModel.inc');
require_once('knjz210nQuery.inc');

class knjz210nController extends Controller {
    var $ModelClassName = "knjz210nModel";
    var $ProgramID      = "KNJZ210N";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "reset":
                case "check":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz210nForm2");
                    break 2;
                case "list":
                case "combo":
                    $this->callView("knjz210nForm1");
                    break 2;
                case "copy":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = "knjz210nindex.php?cmd=list";
                    $args["right_src"] = "knjz210nindex.php?cmd=edit";
                    $args["cols"] = "50%, 50%";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz210nCtl = new knjz210nController;
?>
