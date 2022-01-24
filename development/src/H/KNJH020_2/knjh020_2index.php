<?php

require_once('for_php7.php');

require_once('knjh020_2Model.inc');
require_once('knjh020_2Query.inc');

class knjh020_2Controller extends Controller {
    var $ModelClassName = "knjh020_2Model";
    var $ProgramID        = "KNJH020";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $this->callView("knjh020_2Form2");
                    break 2;
                case "main":
                    $this->callView("knjh020_2Form1");
                    break 2;
                case "add":
					//NO001
					if (!$sessionInstance->auth){
	                    $this->checkAuth(DEF_UPDATE_RESTRICT);
					}
                    $sessionInstance->getInsertModel();
                    $this->callView("knjh020_2Form2");
                    break 2;
                case "update":
					//NO001
					if (!$sessionInstance->auth){
	                    $this->checkAuth(DEF_UPDATE_RESTRICT);
					}
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjh020_2Form2");
                    break 2;
                case "delete":
					//NO001
					if (!$sessionInstance->auth){
	                    $this->checkAuth(DEF_UPDATE_RESTRICT);
					}
                    $sessionInstance->getDeleteModel();
                    $this->callView("knjh020_2Form2");
                    break 2;
                case "apply":
                    $sessionInstance->getApplyModel();
                    $this->callView("knjh020_2Form2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $args["right_src"] = "knjh020_2index.php?cmd=main";
                    $args["edit_src"] = "knjh020_2index.php?cmd=edit";
                    $args["rows"] = "30%,*%";
                    View::frame($args, "frame3.html");
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjh020_2Ctl = new knjh020_2Controller;
//var_dump($_REQUEST);
?>
