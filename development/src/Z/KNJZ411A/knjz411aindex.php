<?php

require_once('for_php7.php');

require_once('knjz411aModel.inc');
require_once('knjz411aQuery.inc');

class knjz411aController extends Controller
{
    public $ModelClassName = "knjz411aModel";
    public $ProgramID      = "KNJZ411A";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                case "chenge_cd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz411aForm2");
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
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "list_from_right":
                    $this->callView("knjz411aForm1");
                    break 2;
                case "search":
                case "sub_search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz411aSubForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = "knjz411aindex.php?cmd=list";
                    $args["right_src"] = "knjz411aindex.php?cmd=edit";
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
$knjz411aCtl = new knjz411aController;
//var_dump($_REQUEST);
