<?php

require_once('for_php7.php');

require_once('knjp741Model.inc');
require_once('knjp741Query.inc');

class knjp741Controller extends Controller {
    var $ModelClassName = "knjp741Model";
    var $ProgramID      = "KNJP741";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                case "curriculum":
                case "class":
                case "add_year":
                case "subclasscd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjp741Form2");
                    break 2;
                case "right_list":
                case "list":
                case "sort":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjp741Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                case "delete2":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    if (trim($sessionInstance->cmd) == "delete"){
                        $sessionInstance->setCmd("list");
                    }else{
                        $sessionInstance->setCmd("edit");
                    }
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP4/index.php?";
                    $args["left_src"] .= "PROGRAMID=".$this->ProgramID;
                    $args["left_src"] .= "&AUTH=".AUTHORITY;
                    $args["left_src"] .= "&name=1";
                    $args["left_src"] .= "&name_kana=1";
                    $args["left_src"] .= "&schno=1";
                    $args["left_src"] .= "&hr_class=1";
                    $args["left_src"] .= "&TARGET=right_frame";
                    $args["left_src"] .= "&PATH=" .urlencode("/P/KNJP741/knjp741index.php?cmd=right");

                    $args["right_src"] = "knjp741index.php?cmd=right";
                    $args["cols"] = "20%,80%";
                    View::frame($args);
                    return;
                case "right":
                    $args["right_src"] = "knjp741index.php?cmd=right_list";
                    $args["edit_src"]  = "knjp741index.php?cmd=edit";
                    $args["rows"] = "35%,*%";
                    View::frame($args,"frame3.html");

                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp741Ctl = new knjp741Controller;
?>
