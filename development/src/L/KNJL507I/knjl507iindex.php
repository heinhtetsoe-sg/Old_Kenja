<?php

require_once('for_php7.php');
require_once('knjl507iModel.inc');
require_once('knjl507iQuery.inc');

class knjl507iController extends Controller
{
    public $ModelClassName = "knjl507iModel";
    public $ProgramID      = "KNJL507I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "reset":
                    $this->callView("knjl507iForm2");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyYearModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "list":
                    $this->callView("knjl507iForm1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = "knjl507iindex.php?cmd=list";
                    $args["right_src"] = "knjl507iindex.php?cmd=edit";
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
$knjl507iCtl = new knjl507iController();
