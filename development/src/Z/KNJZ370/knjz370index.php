<?php

require_once('for_php7.php');

require_once('knjz370Model.inc');
require_once('knjz370Query.inc');

class knjz370Controller extends Controller {
    var $ModelClassName = "knjz370Model";
    var $ProgramID      = "KNJZ370";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "add":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete";
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyYearModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "list":
                case "chgrade":
                    $this->callView("knjz370Form1");
                    break 2;
                case "reset":
                case "edit":
                    $this->callView("knjz370Form2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz370index.php?cmd=list";
                    $args["right_src"] = "knjz370index.php?cmd=edit";
                    $args["cols"] = "45%,*";
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
$knjz370Ctl = new knjz370Controller;
//var_dump($_REQUEST);
?>
