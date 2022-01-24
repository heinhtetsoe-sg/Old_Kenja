<?php

require_once('for_php7.php');

require_once('knjl515hModel.inc');
require_once('knjl515hQuery.inc');

class knjl515hController extends Controller {
    var $ModelClassName = "knjl515hModel";
    var $ProgramID      = "KNJL515H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "changeCmb":
                case "UpdEdit":
                case "edit":
                case "sendLink":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjl515hForm2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("UpdEdit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("UpdEdit");
                    break 1;
                case "list2":
                case "list":
                    $this->callView("knjl515hForm1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjl515hindex.php?cmd=list";
                    $args["right_src"] = "knjl515hindex.php?cmd=edit";
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
$knjl515hCtl = new knjl515hController;
?>
