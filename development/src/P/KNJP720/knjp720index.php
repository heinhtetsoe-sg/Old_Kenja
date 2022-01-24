<?php

require_once('for_php7.php');

require_once('knjp720Model.inc');
require_once('knjp720Query.inc');

class knjp720Controller extends Controller {
    var $ModelClassName = "knjp720Model";
    var $ProgramID      = "KNJP720";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            $sessionInstance->knjp720Model();        //コントロールマスタの呼び出し
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "change":
                    $this->callView("knjp720Form1");
                    break 2;
                case "search":
//                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->schreg_chk($sessionInstance->field["SCHREGNO"]);
                case "edit":
                case "edit2":
                case "reset":
//                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjp720Form2");
                    break 2;
                case "add":
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
//                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
//                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "Ikkatsu":
                case "Ikkatsu2":
//                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjp720Ikkatsu");
                    break 2;
                case "ikkatsu_Insert":
//                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getSubInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("Ikkatsu2");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = "knjp720index.php?cmd=list";
                    $args["right_src"] = "knjp720index.php?cmd=edit";
                    $args["cols"] = "55%,45%";
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
$knjp720Ctl = new knjp720Controller;
?>
