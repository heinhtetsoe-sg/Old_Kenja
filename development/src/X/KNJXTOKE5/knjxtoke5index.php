<?php

require_once('for_php7.php');
require_once('knjxtoke5Model.inc');
require_once('knjxtoke5Query.inc');

class knjxtoke5Controller extends Controller {
    var $ModelClassName = "knjxtoke5Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "error":
                    $this->callView("error");
                    break 2;
                case "main":
                    $this->callView("knjxtoke5Form1");
                    break 2;
                case "read":
                case "tree":
                    $this->callView("knjxtoke5Form2");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]   = "knjxtoke5index.php?cmd=tree";
                    $args["right_src"]  = "knjxtoke5index.php?cmd=main";
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
$knjxtoke5Ctl = new knjxtoke5Controller;
//var_dump($_REQUEST);
?>
