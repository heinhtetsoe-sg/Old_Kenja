<?php

require_once('for_php7.php');

require_once('knjz630Model.inc');
require_once('knjz630Query.inc');
require_once('graph.php');

class knjz630Controller extends Controller {
    var $ModelClassName = "knjz630Model";
    var $ProgramID      = "KNJZ630";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "right_appear":
                case "right_reappear":
                case "right_sanka":
                case "right_change":
                    $this->callView("knjz630Form2");
                    break 2;
                case "list":
                case "left_appear":
                case "left_reappear":
                case "left_sanka":
                case "left_change":
                    $this->callView("knjz630Form1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("comp");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "top":
                    $this->callView("knjz630Form3");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["top_src"] = "knjz630index.php?cmd=top";
                    $args["left_src"] = "knjz630index.php?cmd=list";
                    $args["right_src"] = "knjz630index.php?cmd=edit";
                    $args["cols"] = "50%,50%";
                    $args["rows"] = "0%,10%,*";
                    View::frame($args,  "frame4.html");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz630Ctl = new knjz630Controller;
//var_dump($_REQUEST);
?>
