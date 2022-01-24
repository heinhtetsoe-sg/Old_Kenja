<?php

require_once('for_php7.php');

require_once('knjp712Model.inc');
require_once('knjp712Query.inc');

class knjp712Controller extends Controller {
    var $ModelClassName = "knjp712Model";
    var $ProgramID      = "KNJP712";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            $sessionInstance->knjp712Model();        //コントロールマスタの呼び出し
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "change":
                    $this->callView("knjp712Form1");
                    break 2;
                case "edit":
                case "edit2":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjp712Form2");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("change");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = "knjp712index.php?cmd=list";
                    $args["right_src"] = "knjp712index.php?cmd=edit";
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
$knjp712Ctl = new knjp712Controller;
?>
