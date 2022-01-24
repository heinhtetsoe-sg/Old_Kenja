<?php

require_once('for_php7.php');

require_once('knjz220dModel.inc');
require_once('knjz220dQuery.inc');

class knjz220dController extends Controller {
    var $ModelClassName = "knjz220dModel";
    var $ProgramID      = "KNJZ220D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->copy_data();
                    $this->callView("knjz220dForm2");
                    break 2;
                case "default":
                case "edit":
                case "edit2":
                case "new":
                case "setdef":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz220dForm2");
                    break 2;
                case "edit3":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz220dForm1");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "semester":
                case "list":
                case "chgCmb":
                    $this->callView("knjz220dForm1");
                    break 2;
                case "chgCmb2":
                    $this->callView("knjz220dForm2");
                    break 2;
                case "chgCmb3":
                    $this->callView("knjz220dForm2");
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
                    $args["left_src"] = "knjz220dindex.php?cmd=list";
                    $args["right_src"] = "knjz220dindex.php?cmd=edit";
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
$knjz220dCtl = new knjz220dController;
//var_dump($_REQUEST);
?>
