<?php

require_once('for_php7.php');

require_once('knjh400_kyoukaModel.inc');
require_once('knjh400_kyoukaQuery.inc');
require_once('graph.php');

class knjh400_kyoukaController extends Controller {
    var $ModelClassName = "knjh400_kyoukaModel";
    var $ProgramID      = "KNJH400_KYOUKA";
    
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
                    $this->callView("knjh400_kyoukaForm2");
                    break 2;
                case "list":
                case "left_appear":
                case "left_reappear":
                case "left_sanka":
                case "left_change":
                    $this->callView("knjh400_kyoukaForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("comp");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "top":
                    $this->callView("knjh400_kyoukaForm3");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["top_src"] = "knjh400_kyoukaindex.php?cmd=top";
                    $args["left_src"] = "knjh400_kyoukaindex.php?cmd=list";
                    $args["right_src"] = "knjh400_kyoukaindex.php?cmd=edit";
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
$knjh400_kyoukaCtl = new knjh400_kyoukaController;
//var_dump($_REQUEST);
?>
