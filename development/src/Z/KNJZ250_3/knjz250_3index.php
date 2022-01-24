<?php

require_once('for_php7.php');

require_once('knjz250_3Model.inc');
require_once('knjz250_3Query.inc');

class knjz250_3Controller extends Controller {
    var $ModelClassName = "knjz250_3Model";
    var $ProgramID      = "KNJZ250";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset";
                    $sessionInstance->setAccessLogDetail("S", "KNJZ250_3");
                    $this->callView("knjz250_3Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", "KNJZ250_3");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ250_3");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJZ250_3");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjz250_3Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz250_3index.php?cmd=list";
                    $args["right_src"] = "knjz250_3index.php?cmd=edit&year_code=".VARS::request("year_code");
                    $args["cols"] = "55%,*";
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
$knjz250_3Ctl = new knjz250_3Controller;
//var_dump($_REQUEST);
?>
