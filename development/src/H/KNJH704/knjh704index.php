<?php

require_once('for_php7.php');

require_once('knjh704Model.inc');
require_once('knjh704Query.inc');

class knjh704Controller extends Controller
{
    public $ModelClassName = "knjh704Model";
    public $ProgramID      = "KNJH704";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "change":
                case "reset":
                    $this->callView("knjh704Form2");
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
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjh704Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "change_test":
                    $sessionInstance->knjh704Model(); //コントロールマスタの呼び出し
                    $this->callView("knjh704Form2");
                    exit;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjh704index.php?cmd=list";
                    $args["right_src"] = "knjh704index.php?cmd=edit";
                    $args["cols"] = "52%,48%";
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
$knjh704Ctl = new knjh704Controller();
