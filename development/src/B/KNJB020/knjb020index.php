<?php

require_once('for_php7.php');

require_once('knjb020Model.inc');
require_once('knjb020Query.inc');

class knjb020Controller extends Controller {
    var $ModelClassName = "knjb020Model";
    var $ProgramID      = "KNJB020";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "sel":
                    $this->callView("knjb020Form2");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "list":
                    $this->callView("knjb020Form1");
                    break 2;
                case "clear":
                    $this->callView("knjb020Form2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjb020index.php?cmd=list";
                    $args["right_src"] = "knjb020index.php?cmd=sel";
                    $args["cols"] = "55%,*";
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
$knjb020Ctl = new knjb020Controller;
//var_dump($_REQUEST);
?>
