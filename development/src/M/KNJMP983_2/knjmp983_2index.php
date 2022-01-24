<?php

require_once('for_php7.php');

require_once('knjmp983_2Model.inc');
require_once('knjmp983_2Query.inc');

class knjmp983_2Controller extends Controller {
    var $ModelClassName = "knjmp983_2Model";
    var $ProgramID      = "KNJZ285";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "main":
                case "reset":
                    $this->callView("knjmp983_2Form2");
                    break 2;
                case "add":
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":                
                    $this->callView("knjmp983_2Form1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjmp983_2index.php?cmd=list";
                    $args["right_src"] = "knjmp983_2index.php?cmd=edit";
                    $args["cols"] = "45%,*";
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
$knjmp983_2Ctl = new knjmp983_2Controller;
?>
