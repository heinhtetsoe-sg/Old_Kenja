<?php

require_once('for_php7.php');

require_once('knjg045bModel.inc');
require_once('knjg045bQuery.inc');

class knjg045bController extends Controller {
    var $ModelClassName = "knjg045bModel";
    var $ProgramID      = "KNJG045bB";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "right_list":
                case "from_list":
                case "from_edit":
                    $this->callView("knjg045bForm1");
                    break 2;
                case "edit":
                case "edit2":
                case "updEdit":
                case "updEdit2":
                case "from_right":
                case "reset":
                    $this->callView("knjg045bForm2");
                    break 2;
                case "year":
                case "list":
                    $this->callView("knjg045bForm3");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID); 
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID); 
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knjg045bModel();
                    
                    //分割フレーム作成
                    $args["left_src"]   = "knjg045bindex.php?cmd=list";
                    $args["right_src"]  = "knjg045bindex.php?cmd=right_list";
                    $args["edit_src"]   = "knjg045bindex.php?cmd=edit";
                    $args["cols"] = "25%,75%";
                    $args["rows"] = "50%,50%";
                    View::frame($args,"frame2.html");
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg045bCtl = new knjg045bController;
?>
