<?php

require_once('for_php7.php');

require_once('knjz050wModel.inc');
require_once('knjz050wQuery.inc');

class knjz050wController extends Controller {
    var $ModelClassName = "knjz050wModel";
    var $ProgramID      = "KNJZ050W";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "firstEdit":
                case "edit":
                case "reset":
                    $this->callView("knjz050wForm2");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjz050wForm1");
                    break 2;
                case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz050windex.php?cmd=list";
                    $args["right_src"] = "knjz050windex.php?cmd=firstEdit";
                    $args["cols"] = "50%,50%";
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
$knjz050wCtl = new knjz050wController;
//var_dump($_REQUEST);
?>
