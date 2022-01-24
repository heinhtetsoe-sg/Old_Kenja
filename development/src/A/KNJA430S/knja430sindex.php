<?php

require_once('for_php7.php');

require_once('knja430sModel.inc');
require_once('knja430sQuery.inc');

class knja430sController extends Controller {
    var $ModelClassName = "knja430sModel";
    var $ProgramID      = "KNJA430S";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "new":
                    $sessionInstance->getMaxStampNoModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "add":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "inkan_view":
                case "reset":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "edit":
                    $this->callView("knja430sForm2");
                    break 2;
                case "right_list":
                case "list":
                    $this->callView("knja430sForm1");
                    break 2;
                case "search":
                case "left_list":
                    $this->callView("knja430sForm3");
                    break 2;
                case "search_view":
                    $this->callView("knja430sSearch");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knja430sForm");
                    break 2;
                case "main":
                    $args["left_src"] = "knja430sindex.php?cmd=left_list";
                    $args["right_src"] = "knja430sindex.php?cmd=right_list";
                    $args["edit_src"]  = "knja430sindex.php?cmd=edit";
                    $args["cols"] = "20%,80%";
                    $args["rows"] = "40%,60%";
                    View::frame($args,"frame2.html");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knja430sCtl = new knja430sController;
//var_dump($_REQUEST);
?>
