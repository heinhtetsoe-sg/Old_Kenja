<?php

require_once('for_php7.php');

require_once('knja090mModel.inc');
require_once('knja090mQuery.inc');

class knja090mController extends Controller {

    var $ModelClassName = "knja090mModel";
    var $ProgramID      = "KNJA090M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("list");
                    break 1;
                case "":
                case "list":
                case "copy":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knja090mForm1");
                    break 2;
                case "subform1":
                    $this->callView("knja090mSubForm1");
                    break 2;
                case "subform1_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->GetSubUpdateModel();
                    $sessionInstance->setCmd("subform1");
                    break 1;
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
$knja090mCtl = new knja090mController;
//var_dump($_REQUEST);
?>
