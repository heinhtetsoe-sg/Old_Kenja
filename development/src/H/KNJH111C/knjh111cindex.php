<?php

require_once('for_php7.php');

require_once('knjh111cModel.inc');
require_once('knjh111cQuery.inc');

class knjh111cController extends Controller {
    var $ModelClassName = "knjh111cModel";
    var $ProgramID      = "KNJH111C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "firstEdit":
                case "UpEdit":
                case "edit":
                case "reset":
                    $this->callView("knjh111cForm2");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("UpEdit");
                    break 1;
                case "list":
                    $this->callView("knjh111cForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjh111cindex.php?cmd=list";
                    $args["right_src"] = "knjh111cindex.php?cmd=firstEdit";
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
$knjh111cCtl = new knjh111cController;
//var_dump($_REQUEST);
?>
