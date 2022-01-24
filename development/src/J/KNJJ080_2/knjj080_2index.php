<?php

require_once('for_php7.php');

require_once('knjj080_2Model.inc');
require_once('knjj080_2Query.inc');

class knjj080_2Controller extends Controller {
    var $ModelClassName = "knjj080_2Model";
    var $ProgramID      = "KNJJ080";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", "KNJJ080_2");
                    $this->callView("knjj080_2Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", "KNJJ080_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJJ080_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjj080_2Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJJ080_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjj080_2index.php?cmd=list";
                    $args["right_src"] = "knjj080_2index.php?cmd=edit";
                    $args["cols"] = "50%,*";
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
$knjj080_2Ctl = new knjj080_2Controller;
//var_dump($_REQUEST);
?>
