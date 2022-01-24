<?php

require_once('for_php7.php');
require_once('knjp706cModel.inc');
require_once('knjp706cQuery.inc');

class knjp706cController extends Controller {
    var $ModelClassName = "knjp706cModel";
    var $ProgramID      = "KNJP706C";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                case "form2":   //戻る NO002
                    $this->callView("knjp706cForm2");
                    break 2;
                case "dueUpdate":
                case "paidUpdate":
                case "repayUpdate":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "dueDel":
                case "paidDel":
                case "repayDel":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjp706cForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "right":
                    $args["right_src"] = "knjp706cindex.php?cmd=list";
                    $args["edit_src"] = "knjp706cindex.php?cmd=edit";
                    $args["rows"] = "35%,65%";
                    View::frame($args,"frame3.html");
                    return;
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
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}";
                    $args["left_src"] .= "&PATH=" .urlencode("/P/KNJP706C/knjp706cindex.php?cmd=right");
                    $args["right_src"] = "knjp706cindex.php?cmd=right";
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
$knjp706cCtl = new knjp706cController;
//var_dump($_REQUEST);
?>
