<?php

require_once('for_php7.php');

require_once('knjb3055Model.inc');
require_once('knjb3055Query.inc');

class knjb3055Controller extends Controller {
    var $ModelClassName = "knjb3055Model";
    var $ProgramID      = "KNJB3055";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $this->callView("knjb3055Form2");
                    break 2;
                case "list":
                    $this->callView("knjb3055Form1");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = "knjb3055index.php?cmd=list";
                    $args["right_src"] = "knjb3055index.php?cmd=edit";
                    $args["cols"] = "30%,*";
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
$knjb3055Ctl = new knjb3055Controller;
?>
