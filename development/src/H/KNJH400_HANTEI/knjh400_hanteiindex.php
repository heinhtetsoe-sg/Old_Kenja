<?php

require_once('for_php7.php');

require_once('knjh400_hanteiModel.inc');
require_once('knjh400_hanteiQuery.inc');

class knjh400_hanteiController extends Controller {
    var $ModelClassName = "knjh400_hanteiModel";
    var $ProgramID      = "KNJH400_HANTEI";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjh400_hanteiForm1");
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
                    $args["right_src"] = "knjh400_hanteiindex.php?cmd=list";
                    $args["edit_src"] = "knjh400_hanteiindex.php?cmd=edit";
                    $args["rows"] = "115px,*";
                    View::frame($args,  "frame3.html");
                    exit;
                case "edit";
                case "reappear";
                    $this->callView("knjh400_hanteiForm1");
                    break 2;
                case "list";
                    $this->callView("knjh400_hanteiForm2");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjh400_hanteiCtl = new knjh400_hanteiController;
?>
