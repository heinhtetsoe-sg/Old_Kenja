<?php

require_once('for_php7.php');

require_once('knjz056Model.inc');
require_once('knjz056Query.inc');

class knjz056Controller extends Controller {
    var $ModelClassName = "knjz056Model";
    var $ProgramID      = "KNJZ056";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "course":
                case "edit":
                    $this->callView("knjz056Form2");
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
                case "reset":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjz056Form1");
                    break 2;
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
                    $args["left_src"] = "knjz056index.php?cmd=list";
                    $args["right_src"] = "knjz056index.php?cmd=edit";
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
$knjz056Ctl = new knjz056Controller;
//var_dump($_REQUEST);
?>
