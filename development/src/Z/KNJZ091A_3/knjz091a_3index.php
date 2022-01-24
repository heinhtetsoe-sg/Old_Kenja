<?php

require_once('for_php7.php');

require_once('knjz091a_3Model.inc');
require_once('knjz091a_3Query.inc');

class knjz091a_3Controller extends Controller {
    var $ModelClassName = "knjz091a_3Model";
    var $ProgramID      = "KNJZ091A";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ091A_3");
                    $this->callView("knjz091a_3Form2");
                    break 2;
                case "rosen":
                case "get_rosen":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz091a_3Rosen");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", "KNJZ091A_3");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ091A_3");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ091A_3");
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjz091a_3Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJZ091A_3");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz091a_3index.php?cmd=list";
                    $args["right_src"] = "knjz091a_3index.php?cmd=edit";
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
$knjz091a_3Ctl = new knjz091a_3Controller;
//var_dump($_REQUEST);
?>
