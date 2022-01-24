<?php

require_once('for_php7.php');

require_once('knjmp715Model.inc');
require_once('knjmp715Query.inc');

class knjmp715Controller extends Controller {
    var $ModelClassName = "knjmp715Model";
    var $ProgramID      = "KNJMP715";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "right":
                case "patternEdit":
                case "add":
                case "updEdit":
                case "printEdit":
                    $this->callView("knjmp715Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "print":
                    $sessionInstance->getPrintModel();
                    $sessionInstance->setCmd("printEdit");
                    break 1;
                case "list":
                case "changeYear":
                    $this->callView("knjmp715Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP4/index.php?";
                    $args["left_src"] .= "PROGRAMID=".$this->ProgramID;
                    $args["left_src"] .= "&AUTH=".AUTHORITY;
                    $args["left_src"] .= "&search_div=1";
                    $args["left_src"] .= "&name=1";
                    $args["left_src"] .= "&name_kana=1";
                    $args["left_src"] .= "&ent_year=1";
                    $args["left_src"] .= "&grd_year=1";
                    $args["left_src"] .= "&grade=1";
                    $args["left_src"] .= "&hr_class=1";
                    $args["left_src"] .= "&schno=1";
                    $args["left_src"] .= "&TARGET=right_frame";
                    $args["left_src"] .= "&PATH=" .urlencode("/M/KNJMP715/knjmp715index.php?cmd=edit");
                    $args["right_src"] = "knjmp715index.php?cmd=edit";

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
$knjmp715Ctl = new knjmp715Controller;
//var_dump($_REQUEST);
?>
