<?php

require_once('for_php7.php');

require_once('knjj010_3Model.inc');
require_once('knjj010_3Query.inc');
class knjj010_3Controller extends Controller
{
    public $ModelClassName = "knjj010_3Model";
    public $ProgramID      = "KNJJ010";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                    $sessionInstance->setAccessLogDetail("S", "KNJJ010_3");
                    $this->callView("knjj010_3Form2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJJ010_3");
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "updateCopy":
                    $sessionInstance->setAccessLogDetail("U", "KNJJ010_3");
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("list");
                    break 1;
                case "list":
                case "changeYear":
                    $sessionInstance->setAccessLogDetail("S", "KNJJ010_3");
                    $this->callView("knjj010_3Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjj010_3index.php?cmd=list";
                    $args["right_src"] = "knjj010_3index.php?cmd=edit";
                    $args["cols"] = "50%,*%";
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
$knjj010_3Ctl = new knjj010_3Controller();
