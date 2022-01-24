<?php

require_once('for_php7.php');

require_once('knjj201_2Model.inc');
require_once('knjj201_2Query.inc');

class knjj201_2Controller extends Controller {
    var $ModelClassName = "knjj201_2Model";
    var $ProgramID      = "KNJJ201";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", "KNJJ201_2");
                    $this->callView("knjj201_2Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", "KNJJ201_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJJ201_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":                
                    $this->callView("knjj201_2Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJJ201_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjj201_2index.php?cmd=list";
                    $args["right_src"] = "knjj201_2index.php?cmd=edit";
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
$knjj201_2Ctl = new knjj201_2Controller;
//var_dump($_REQUEST);
?>
