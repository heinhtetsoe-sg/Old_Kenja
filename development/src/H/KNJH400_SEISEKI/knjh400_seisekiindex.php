<?php

require_once('for_php7.php');

require_once('knjh400_seisekiModel.inc');
require_once('knjh400_seisekiQuery.inc');
require_once('graph.php');

class knjh400_seisekiController extends Controller {
    var $ModelClassName = "knjh400_seisekiModel";
    var $ProgramID      = "KNJH400_SEISEKI";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjh400_seisekiForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "selectclass":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->selectclass();
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["right_src"] = "knjh400_seisekiindex.php?cmd=list";
                    $args["edit_src"] = "knjh400_seisekiindex.php?cmd=edit";
                    $args["rows"] = "80px,*";
                    View::frame($args,  "frame3.html");
                    exit;
                case "edit";
                case "reappear";
                case "syubetu_change";
                case "sanka";
                case "all";
                    $this->callView("knjh400_seisekiForm1");
                    break 2;
                case "list";
                    $this->callView("knjh400_seisekiForm2");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjh400_seisekiCtl = new knjh400_seisekiController;
?>
