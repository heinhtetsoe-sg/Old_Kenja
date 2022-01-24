<?php

require_once('for_php7.php');

require_once('knjh410_hanteiModel.inc');
require_once('knjh410_hanteiQuery.inc');

class knjh410_hanteiController extends Controller {
    var $ModelClassName = "knjh410_hanteiModel";
    var $ProgramID      = "KNJH410_HANTEI";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjh410_hanteiForm1");
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
                    $args["right_src"] = "knjh410_hanteiindex.php?cmd=list";
                    $args["edit_src"] = "knjh410_hanteiindex.php?cmd=edit";
                    $args["rows"] = "115px,*";
                    View::frame($args,  "frame3.html");
                    exit;
                case "edit";
                case "reappear";
                    $this->callView("knjh410_hanteiForm1");
                    break 2;
                case "list";
                    $this->callView("knjh410_hanteiForm2");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjh410_hanteiCtl = new knjh410_hanteiController;
?>
