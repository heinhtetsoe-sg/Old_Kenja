<?php

require_once('for_php7.php');

require_once('knjz091_2Model.inc');
require_once('knjz091_2Query.inc');

class knjz091_2Controller extends Controller {
    var $ModelClassName = "knjz091_2Model";
    var $ProgramID      = "KNJZ091";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ091_2");
                    $this->callView("knjz091_2Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", "KNJZ091_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ091_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ091_2");
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjz091_2Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJZ091_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz091_2index.php?cmd=list";
                    $args["right_src"] = "knjz091_2index.php?cmd=edit";
                    $args["cols"] = "25%,75%";
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
$knjz091_2Ctl = new knjz091_2Controller;
//var_dump($_REQUEST);
?>
