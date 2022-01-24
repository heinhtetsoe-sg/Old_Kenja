<?php

require_once('for_php7.php');

require_once('knjz220Model.inc');
require_once('knjz220Query.inc');

class knjz220Controller extends Controller {
    var $ModelClassName = "knjz220Model";
    var $ProgramID      = "KNJZ220";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->copy_data();
                    $this->callView("knjz220Form2");
                    break 2;
                case "default":
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz220Form2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjz220Form1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz220index.php?cmd=list";
                    $args["right_src"] = "knjz220index.php?cmd=edit";
                    $args["cols"] = "30%,50%";
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
$knjz220Ctl = new knjz220Controller;
//var_dump($_REQUEST);
?>
