<?php

require_once('for_php7.php');

require_once('knje040Model.inc');
require_once('knje040Query.inc');

class knje040Controller extends Controller {
    var $ModelClassName = "knje040Model";
    var $ProgramID      = "knje040";
    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                    $this->callView("knje040Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knje040index.php?cmd=list";
                    $args["right_src"] = REQUESTROOT ."/X/KNJXATTEND/index.php?MEMO=knje040";
                    $args["cols"] = "50%,50%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje040Ctl = new knje040Controller;
//var_dump($_REQUEST);
?>
