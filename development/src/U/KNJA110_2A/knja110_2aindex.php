<?php

require_once('for_php7.php');

require_once('knja110_2aModel.inc');
require_once('knja110_2aQuery.inc');

class knja110_2aController extends Controller {
    var $ModelClassName = "knja110_2aModel";
    var $ProgramID      = "KNJA110A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list2":
                    $this->callView("knja110_2aForm1");
                   break 2;
                case "list":
                    $this->callView("knja110_2aForm1");
                   break 2;
                case "edit":
                case "clear":
                    $this->callView("knja110_2aForm2");
                    break 2;
                case "subForm1":
                    $this->callView("knja110_2aSubForm1");
                    break 2;
                case "rireki":
                case "histEdit":
                case "changeCmb":
                    $this->callView("knja110_2aSubHist");
                    break 2;
                case "histAdd":
                case "histUpd":
                case "histDel":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateHistModel();
                    break 1;
                case "rireki2":
                case "histEdit2":
                case "changeCmb2":
                    $this->callView("knja110_2aSubHistGuarantor");
                    break 2;
                case "histAdd2":
                case "histUpd2":
                case "histDel2":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateHistModel2();
                    break 1;
                case "add":
                case "add2":
                case "add4":
                case "add5":
                case "add6":
                case "subAdd":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                case "update2":
                case "update4":
                case "update5":
                case "update6":
                case "subUpdate":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                case "delete2":
                case "delete4":
                case "delete5":
                case "delete6":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "guardian_hist_dat_kousin":
                    $this->callView("knja110_2aSubForm1");
                    break 2;
                case "":
                case "edit2":
                case "back":
                    $args["right_src"] = "knja110_2aindex.php?cmd=list&SCHREGNO=".$sessionInstance->schregno;
                    $args["edit_src"]  = "knja110_2aindex.php?cmd=edit&SCHREGNO=".$sessionInstance->schregno;
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
$knja110_2aCtl = new knja110_2aController;
?>
