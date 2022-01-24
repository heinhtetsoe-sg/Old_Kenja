<?php

require_once('for_php7.php');

require_once('knjz234aModel.inc');
require_once('knjz234aQuery.inc');

class knjz234aController extends Controller
{
    public $ModelClassName = "knjz234aModel";
    public $ProgramID      = "KNJZ234A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change":
                case "sel":
                    $this->callView("knjz234aForm2");
                    break 2;
                case "insert":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "course":
                    $this->callView("knjz234aForm1");
                    break 2;
                case "list":
                    $this->callView("knjz234aForm1");
                    break 2;
                case "init":
                    $this->callView("knjz234aForm2");
                    break 2;
                case "clear":
                    $this->callView("knjz234aForm2");
                    break 2;
                case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz234aindex.php?cmd=list";
                    $args["right_src"] = "knjz234aindex.php?cmd=sel";
                    $args["cols"] = "47%,*";
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
$knjz234aCtl = new knjz234aController();
//var_dump($_REQUEST);
