<?php

require_once('for_php7.php');

require_once('knjb0055Model.inc');
require_once('knjb0055Query.inc');

class knjb0055Controller extends Controller {
    var $ModelClassName = "knjb0055Model";
    var $ProgramID      = "KNJB0055";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $this->callView("knjb0055Form2");
                    break 2;
                case "list":
                    $this->callView("knjb0055Form1");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = "knjb0055index.php?cmd=list";
                    $args["right_src"] = "knjb0055index.php?cmd=edit";
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
$knjb0055Ctl = new knjb0055Controller;
?>
