<?php

require_once('for_php7.php');

require_once('knja110_2Model.inc');
require_once('knja110_2Query.inc');

class knja110_2Controller extends Controller {
    var $ModelClassName = "knja110_2Model";
    var $ProgramID      = "KNJA110";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                    $this->callView("knja110_2Form1");
                   break 2;
                case "edit":
                case "clear":
                    $this->callView("knja110_2Form2");
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
                case "":
                    $args["right_src"] = "knja110_2index.php?cmd=list&SCHREGNO=".$sessionInstance->schregno;
                    $args["edit_src"]  = "knja110_2index.php?cmd=edit&SCHREGNO=".$sessionInstance->schregno;
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
$knja110_2Ctl = new knja110_2Controller;
?>
