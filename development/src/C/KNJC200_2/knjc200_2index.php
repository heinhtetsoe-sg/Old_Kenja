<?php

require_once('for_php7.php');

require_once('knjc200_2Model.inc');
require_once('knjc200_2Query.inc');

class knjc200_2Controller extends Controller {
    var $ModelClassName = "knjc200_2Model";
    var $ProgramID      = "KNJC200";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":  //更新
                    $sessionInstance->setAccessLogDetail("U", "KNJC200_2"); 
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("schno");
                    break 1;
                case "delete":  //削除
                    $sessionInstance->setAccessLogDetail("D", "KNJC200_2"); 
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("schno");
                    break 1;
                case "":
                case "schno":
                    $sessionInstance->setAccessLogDetail("S", "KNJC200_2"); 
                    $sessionInstance->knjc200_2Model();     //コントロールマスタの呼び出し
                    $this->callView("knjc200_2Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjc200_2Ctl = new knjc200_2Controller;
//var_dump($_REQUEST);
?>
