<?php

require_once('for_php7.php');
require_once('knjlz001Model.inc');
require_once('knjlz001Query.inc');

class knjlz001Controller extends Controller
{
    public $ModelClassName = "knjlz001Model";
    public $ProgramID      = "KNJLZ001";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", "KNJLZ001");
                    $this->callView("knjlz001Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", "KNJLZ001");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJLZ001");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "change":
                    $sessionInstance->setAccessLogDetail("S", "KNJLZ001");
                    $this->callView("knjlz001Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJLZ001");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjlz001index.php?cmd=list";
                    $args["right_src"] = "knjlz001index.php?cmd=edit";
                    $args["cols"] = "39%,61%";
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
$knjlz001Ctl = new knjlz001Controller();
//var_dump($_REQUEST);
