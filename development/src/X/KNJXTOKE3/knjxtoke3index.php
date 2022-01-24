<?php

require_once('for_php7.php');

require_once('knjxtoke3Model.inc');
require_once('knjxtoke3Query.inc');

class knjxtoke3Controller extends Controller {
    var $ModelClassName = "knjxtoke3Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "error":
                    $this->callView("error");
                    break 2;
                case "main":
                    $this->callView("knjxtoke3Form1");
                    break 2;
                case "tree":
                case "read":
                    $this->callView("knjxtoke3Form2");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]   = "knjxtoke3index.php?cmd=tree";
                    $args["right_src"]  = "knjxtoke3index.php?cmd=main";
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
$knjxtoke3Ctl = new knjxtoke3Controller;
//var_dump($_REQUEST);
?>
