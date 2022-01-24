<?php

require_once('for_php7.php');

require_once('knjz051_3aModel.inc');
require_once('knjz051_3aQuery.inc');

class knjz051_3aController extends Controller {
    var $ModelClassName = "knjz051_3aModel";
    var $ProgramID      = "KNJZ051A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ051_3A");
                    $this->callView("knjz051_3aForm2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ051_3A");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "chg_year":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ051_3A");
                    $this->callView("knjz051_3aForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz051_3aindex.php?cmd=list";
                    $args["right_src"] = "knjz051_3aindex.php?cmd=edit";
                    $args["cols"] = "50%,*";
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
$knjz051_3aCtl = new knjz051_3aController;
?>
