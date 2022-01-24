<?php

require_once('for_php7.php');

require_once('knjh410_kyoukaModel.inc');
require_once('knjh410_kyoukaQuery.inc');
require_once('graph.php');

class knjh410_kyoukaController extends Controller {
    var $ModelClassName = "knjh410_kyoukaModel";
    var $ProgramID      = "KNJH410_KYOUKA";
    
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
                    $this->callView("knjh410_kyoukaForm2");
                    break 2;
                case "list":
                case "left_appear":
                case "left_reappear":
                case "left_sanka":
                case "left_change":
                    $this->callView("knjh410_kyoukaForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("comp");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "top":
                    $this->callView("knjh410_kyoukaForm3");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["top_src"] = "knjh410_kyoukaindex.php?cmd=top";
                    $args["left_src"] = "knjh410_kyoukaindex.php?cmd=list";
                    $args["right_src"] = "knjh410_kyoukaindex.php?cmd=edit";
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
$knjh410_kyoukaCtl = new knjh410_kyoukaController;
//var_dump($_REQUEST);
?>
