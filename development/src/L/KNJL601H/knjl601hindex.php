<?php
require_once('knjl601hModel.inc');
require_once('knjl601hQuery.inc');

class knjl601hController extends Controller
{
    public $ModelClassName = "knjl601hModel";
    public $ProgramID      = "KNJL601H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $this->callView("knjl601hForm2");
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
                    $this->callView("knjl601hForm1");
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
                    $args["left_src"] = "knjl601hindex.php?cmd=list";
                    $args["right_src"] = "knjl601hindex.php?cmd=edit";
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
$knjl601hCtl = new knjl601hController();
