<?php

require_once('for_php7.php');

require_once('knjz220cModel.inc');
require_once('knjz220cQuery.inc');

class knjz220cController extends Controller {
    var $ModelClassName = "knjz220cModel";
    var $ProgramID      = "KNJZ220C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->copy_data();
                    $this->callView("knjz220cForm2");
                    break 2;
                case "default":
                case "edit":
                case "new":
                case "setdef":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz220cForm2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "semester":
                case "list":
                    $this->callView("knjz220cForm1");
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
                    $args["left_src"] = "knjz220cindex.php?cmd=list";
                    $args["right_src"] = "knjz220cindex.php?cmd=edit";
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
$knjz220cCtl = new knjz220cController;
//var_dump($_REQUEST);
?>
