<?php

require_once('for_php7.php');

require_once('knjxgtreModel.inc');
require_once('knjxgtreQuery.inc');

class knjxgtreController extends Controller {
    var $ModelClassName = "knjxgtreModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "error":
                    $this->callView("error");
                    break 2;
                case "main":
                case "init":
                case "left":
                    $this->callView("knjxgtreForm2");
                    break 2;
                case "tree":
                    $this->callView("knjxgtreForm1");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]   = "index.php?cmd=tree";
                    $args["right_src"]  = "index.php?cmd=main";
                    $args["cols"] = "25%,*";
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
$knjxgtreCtl = new knjxgtreController;
//var_dump($_REQUEST);
?>
