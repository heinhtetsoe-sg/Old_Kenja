<?php

require_once('for_php7.php');

require_once('knjz410aModel.inc');
require_once('knjz410aQuery.inc');

class knjz410aController extends Controller {
    var $ModelClassName = "knjz410aModel";
    var $ProgramID      = "KNJZ410A";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                case "chenge_cd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz410aForm2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjz410aForm1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = "knjz410aindex.php?cmd=list";
                    $args["right_src"] = "knjz410aindex.php?cmd=edit";
                    $args["cols"] = "50%,50%";
                    View::frame($args);
                    exit;
                case "add_addr_edit":
                case "add_addr_reset":
                case "add_addr_chenge_cd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz410aSubForm2");
                    break 2;
                case "add_addr_add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("add_addr_edit");
                    break 1;
                case "add_addr_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("add_addr_edit");
                    break 1;
                case "add_addr_list":
                    $this->callView("knjz410aSubForm1");
                    break 2;
                case "add_addr_delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("add_addr_edit");
                    break 1;
                case "add_addr":
                    //分割フレーム作成
                    $args["left_src"] = "knjz410aindex.php?cmd=add_addr_list";
                    $args["right_src"] = "knjz410aindex.php?cmd=add_addr_edit";
                    $args["cols"] = "50%,50%";
                    View::frame($args);
                    exit;
                    $this->callView("knjz410aSubForm2");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz410aCtl = new knjz410aController;
//var_dump($_REQUEST);
?>
