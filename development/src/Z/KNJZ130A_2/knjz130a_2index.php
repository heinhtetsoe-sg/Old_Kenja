<?php

require_once('for_php7.php');

require_once('knjz130a_2Model.inc');
require_once('knjz130a_2Query.inc');

class knjz130a_2Controller extends Controller {
    var $ModelClassName = "knjz130a_2Model";
    var $ProgramID      = "KNJZ130";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ130A_2");
                    $this->callView("knjz130a_2Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", "KNJZ130A_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ130A_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ130A_2");
                    $this->callView("knjz130a_2Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJZ130A_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz130a_2index.php?cmd=list";
                    $args["right_src"] = "knjz130a_2index.php?cmd=edit";
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
$knjz130a_2Ctl = new knjz130a_2Controller;
//var_dump($_REQUEST);
?>
