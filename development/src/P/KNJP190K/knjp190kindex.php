<?php

require_once('for_php7.php');

require_once('knjp190kModel.inc');
require_once('knjp190kQuery.inc');

class knjp190kController extends Controller {
    var $ModelClassName = "knjp190kModel";
    var $ProgramID      = "knjp190k";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                    $this->callView("knjp190kForm2");
                    break 2;
                case "right_list":
                case "list":
                    $this->callView("knjp190kForm1");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":

                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXPK/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/P/KNJP190K/knjp190kindex.php?cmd=right_list") ."&button=1";
                    $args["right_src"] = "knjp190kindex.php?cmd=right_list";
                    $args["edit_src"]  = "knjp190kindex.php?cmd=edit";
                    $args["cols"] = "20%,80%";
#                    $args["cols"] = "24%,76%";
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
$knjp190kCtl = new knjp190kController;
?>
