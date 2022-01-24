<?php

require_once('for_php7.php');

require_once('knjz392_2Model.inc');
require_once('knjz392_2Query.inc');

class knjz392_2Controller extends Controller {
    var $ModelClassName = "knjz392_2Model";
    var $ProgramID      = "KNJZ392";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ392_2");
                    $this->callView("knjz392_2Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", "KNJZ392_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ392_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":                
                    $this->callView("knjz392_2Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJZ392_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz392_2index.php?cmd=list";
                    $args["right_src"] = "knjz392_2index.php?cmd=edit";
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
$knjz392_2Ctl = new knjz392_2Controller;
//var_dump($_REQUEST);
?>
