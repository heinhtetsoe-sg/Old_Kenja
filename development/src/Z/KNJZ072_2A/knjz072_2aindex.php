<?php

require_once('for_php7.php');

require_once('knjz072_2aModel.inc');
require_once('knjz072_2aQuery.inc');

class knjz072_2aController extends Controller
{
    public $ModelClassName = "knjz072_2aModel";
    public $ProgramID      = "KNJZ072A";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "edit":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ072_2A");
                    $this->callView("knjz072_2aForm2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", "KNJZ072_2A");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ072_2A");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "list":
                case "change":
                    $this->callView("knjz072_2aForm1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJZ072_2A");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz072_2aindex.php?cmd=list&year_code=".VARS::request("year_code");
                    $args["right_src"] = "knjz072_2aindex.php?cmd=edit&year_code=".VARS::request("year_code");
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
$knjz072_2aCtl = new knjz072_2aController;
//var_dump($_REQUEST);
