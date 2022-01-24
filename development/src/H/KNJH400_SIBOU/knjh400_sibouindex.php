<?php

require_once('for_php7.php');

require_once('knjh400_sibouModel.inc');
require_once('knjh400_sibouQuery.inc');

class knjh400_sibouController extends Controller {
    var $ModelClassName = "knjh400_sibouModel";
    var $ProgramID      = "KNJH400_SIBOU";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjh400_sibouForm1");
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
                    $args["right_src"] = "knjh400_sibouindex.php?cmd=list";
                    $args["edit_src"] = "knjh400_sibouindex.php?cmd=edit";
                    $args["rows"] = "115px,*";
                    View::frame($args,  "frame3.html");
                    exit;
                case "edit";
                case "reappear";
                    $this->callView("knjh400_sibouForm1");
                    break 2;
                case "list";
                    $this->callView("knjh400_sibouForm2");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjh400_sibouCtl = new knjh400_sibouController;
?>
