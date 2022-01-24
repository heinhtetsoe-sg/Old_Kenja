<?php

require_once('for_php7.php');
require_once('knjz334Model.inc');
require_once('knjz334Query.inc');

class knjz334Controller extends Controller {
    var $ModelClassName = "knjz334Model";
    var $ProgramID      = "KNJZ334";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "comp":
                case "choice":
                case "false":
                case "indiv":
                case "grp":
                case "change_grp":
                    $this->callView("knjz334Form2");
                    break 2;
                case "add":
                case "indadd":
                    if($sessionInstance->getInsertModel()){
                        $sessionInstance->setCmd("comp");
                        break 1;
                    }else{
                        $sessionInstance->setCmd("edit");
                        break 1;
                    }
                case "update":
                case "indupdate":
                    if($sessionInstance->getUpdateModel()){
                        $sessionInstance->setCmd("comp");
                        break 1;
                    }else{
                        $sessionInstance->setCmd("false");
                        break 1;
                    }
                case "execute":
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("choice");
                    break 1;
                case "executeDel":
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getExecDelModel();
                    $sessionInstance->setCmd("choice");
                    break 1;
                case "reset":
                    $this->callView("knjz334Form2");
                    break 2;
                case "list":
                    $this->callView("knjz334Form1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("comp");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz334index.php?cmd=list";
                    $args["right_src"] = "knjz334index.php?cmd=edit";
                    $args["cols"] = "40%,60%";
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
$knjz334Ctl = new knjz334Controller;
//var_dump($_REQUEST);
?>
