<?php

require_once('for_php7.php');

require_once('knja110_2bModel.inc');
require_once('knja110_2bQuery.inc');

class knja110_2bController extends Controller {
    var $ModelClassName = "knja110_2bModel";
    var $ProgramID      = "KNJA110B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list2":
                    $this->callView("knja110_2bForm1");
                   break 2;
                case "list":
                    $this->callView("knja110_2bForm1");
                   break 2;
                case "edit":
                case "clear":
                    $this->callView("knja110_2bForm2");
                    break 2;
                case "subForm1":
                    $this->callView("knja110_2bSubForm1");
                    break 2;
                case "rireki":
                case "histEdit":
                case "changeCmb":
                    $this->callView("knja110_2bSubHist");
                    break 2;
                case "histAdd":
                case "histUpd":
                case "histDel":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateHistModel();
                    break 1;
                case "add":
                case "add2":
                case "subAdd":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                case "update2":
                case "subUpdate":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                case "delete2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "guardian_hist_dat_kousin":
                    $this->callView("knja110_2bSubForm1");
                    break 2;
                case "":
                case "edit2":
                case "back":
                    $args["right_src"] = "knja110_2bindex.php?cmd=list&SCHREGNO=".$sessionInstance->schregno;
                    $args["edit_src"]  = "knja110_2bindex.php?cmd=edit&SCHREGNO=".$sessionInstance->schregno;
                    $args["rows"] = "22%,*%";
                    View::frame($args,"frame3.html");
                    return;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knja110_2bCtl = new knja110_2bController;
?>
