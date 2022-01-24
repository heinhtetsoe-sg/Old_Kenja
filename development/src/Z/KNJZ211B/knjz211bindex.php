<?php

require_once('for_php7.php');

require_once('knjz211bModel.inc');
require_once('knjz211bQuery.inc');

class knjz211bController extends Controller {
    var $ModelClassName = "knjz211bModel";
    var $ProgramID      = "KNJZ211B";
    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "kakutei":
                case "edit":
                case "reset":
                case "main":
                case "chenge_cd":
                    $this->callView("knjz211bForm2");
                    break 2;
                case "list":
                    $this->callView("knjz211bForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("chenge_cd");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz211bindex.php?cmd=list";
                    $args["right_src"] = "knjz211bindex.php?cmd=edit";
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
$knjz211bCtl = new knjz211bController;
//var_dump($_REQUEST);
?>
