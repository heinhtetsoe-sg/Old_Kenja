<?php

require_once('for_php7.php');

require_once('knjxtoke4Model.inc');
require_once('knjxtoke4Query.inc');

class knjxtoke4Controller extends Controller {
    var $ModelClassName = "knjxtoke4Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "error":
                    $this->callView("error");
                    break 2;
                case "main":
                    $this->callView("knjxtoke4Form1");
                    break 2;
                case "read":
                case "tree":
                    $this->callView("knjxtoke4Form2");
                    break 2;
                case "toukei":
                case "left":
                    $this->callView("left");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]   = "knjxtoke4index.php?cmd=tree";
                    $args["right_src"]  = "knjxtoke4index.php?cmd=main";
                    $args["cols"] = "30%,*";
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
$knjxtoke4Ctl = new knjxtoke4Controller;
//var_dump($_REQUEST);
?>
