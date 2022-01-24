<?php

require_once('for_php7.php');

require_once('knjh210_2Model.inc');
require_once('knjh210_2Query.inc');
echo $y;
class knjh210_2Controller extends Controller {
    var $ModelClassName = "knjh210_2Model";
    var $ProgramID      = "KNJH210";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                    $sessionInstance->setAccessLogDetail("S", "KNJH210_2");
                    $this->callView("knjh210_2Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", "KNJH210_2");
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJH210_2");
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $sessionInstance->setAccessLogDetail("S", "KNJH210_2");
                    $this->callView("knjh210_2Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJH210_2");
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjh210_2index.php?cmd=list";
                    $args["right_src"] = "knjh210_2index.php?cmd=edit";
                    $args["cols"] = "55%,*%";
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
$knjh210_2Ctl = new knjh210_2Controller;
//var_dump($_REQUEST);
?>
